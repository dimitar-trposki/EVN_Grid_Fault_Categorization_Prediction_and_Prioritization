package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ExportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ExportBatchResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.UnauthorizedException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.*;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.BatchStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.*;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.ExportService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// TODO: wire audit log once Module 18 is complete
// TODO: add streaming / paging for large exports
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final ExportBatchRepository exportBatchRepository;
    private final FaultReportRepository faultReportRepository;
    private final InterventionRepository interventionRepository;

    @Value("${app.export.storage-path:./exports}")
    private String storagePath;

    @Override
    @Transactional
    public ExportBatchResponse exportFaults(ExportRequest request, Long userId) {
        validateFormat(request.format());
        List<FaultReport> faults = faultReportRepository.findAll();
        return doExport(request, userId, "faults", os -> {
            if (isCsv(request)) writeFaultsCsv(faults, os);
            else writeFaultsXlsx(faults, os);
        });
    }

    @Override
    @Transactional
    public ExportBatchResponse exportInterventions(ExportRequest request, Long userId) {
        validateFormat(request.format());
        List<Intervention> interventions = interventionRepository.findAll();
        return doExport(request, userId, "interventions", os -> {
            if (isCsv(request)) writeInterventionsCsv(interventions, os);
            else writeInterventionsXlsx(interventions, os);
        });
    }

    @Override
    @Transactional
    public ExportBatchResponse exportAnalytics(ExportRequest request, Long userId) {
        validateFormat(request.format());
        List<FaultReport> faults = faultReportRepository.findAll();
        Map<FaultPriority, Long> byPriority = faults.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getFaultPriority() != null ? f.getFaultPriority() : FaultPriority.LOW,
                        Collectors.counting()));
        long totalInterventions = interventionRepository.count();

        return doExport(request, userId, "analytics", os -> {
            if (isCsv(request)) writeAnalyticsCsv(byPriority, faults.size(), totalInterventions, os);
            else writeAnalyticsXlsx(byPriority, faults.size(), totalInterventions, os);
        });
    }

    @Override
    public Resource getFile(Long batchId, User requestingUser) {
        ExportBatch batch = exportBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("ExportBatch", batchId));

        boolean isOwner = Objects.equals(batch.getCreatedByUserId(), requestingUser.getId());
        boolean isAdmin = requestingUser.getUserRole() == RoleType.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("You do not have permission to download this file");
        }

        if (batch.getFilePath() == null || batch.getExportStatus() == BatchStatus.FAILED) {
            throw new ResourceNotFoundException("Export file is not available for batch " + batchId);
        }

        Path path = Paths.get(batch.getFilePath());
        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Export file not found on disk for batch " + batchId);
        }

        return new FileSystemResource(path.toFile());
    }

    // --- Internal write orchestration ---

    @FunctionalInterface
    private interface OutputWriter {
        void write(OutputStream os) throws IOException;
    }

    private ExportBatchResponse doExport(ExportRequest request, Long userId, String prefix, OutputWriter writer) {
        Path filePath = null;
        BatchStatus status;
        try {
            filePath = writeToFile(request, prefix, userId, writer);
            status = BatchStatus.COMPLETED;
        } catch (Exception e) {
            status = BatchStatus.FAILED;
        }
        return saveBatch(request, userId, filePath, status);
    }

    private Path writeToFile(ExportRequest request, String prefix, Long userId, OutputWriter writer) throws IOException {
        Path dir = Paths.get(storagePath);
        Files.createDirectories(dir);
        String ext = isCsv(request) ? ".csv" : ".xlsx";
        String filename = prefix + "_" + userId + "_" + System.currentTimeMillis() + ext;
        Path filePath = dir.resolve(filename);
        try (OutputStream os = Files.newOutputStream(filePath)) {
            writer.write(os);
        }
        return filePath;
    }

    // --- CSV writers ---

    private void writeFaultsCsv(List<FaultReport> faults, OutputStream os) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "title", "description", "faultPriority", "faultClassification",
                                "locationId", "locationAddress")
                        .build())) {
            for (FaultReport f : faults) {
                printer.printRecord(
                        f.getId(), f.getTitle(), f.getDescription(),
                        f.getFaultPriority(), f.getFaultClassification(),
                        f.getLocation().getId(), f.getLocation().getAddress());
            }
        }
    }

    private void writeInterventionsCsv(List<Intervention> interventions, OutputStream os) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder()
                        .setHeader("id", "description", "faultReportId", "crewId")
                        .build())) {
            for (Intervention i : interventions) {
                printer.printRecord(
                        i.getId(), i.getDescription(),
                        i.getFaultReport().getId(), i.getCrew().getId());
            }
        }
    }

    private void writeAnalyticsCsv(Map<FaultPriority, Long> byPriority, long totalFaults,
                                    long totalInterventions, OutputStream os) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(
                new OutputStreamWriter(os, StandardCharsets.UTF_8),
                CSVFormat.DEFAULT.builder().setHeader("metric", "value").build())) {
            printer.printRecord("totalFaults", totalFaults);
            printer.printRecord("totalInterventions", totalInterventions);
            for (Map.Entry<FaultPriority, Long> e : byPriority.entrySet()) {
                printer.printRecord("faults_" + e.getKey().name(), e.getValue());
            }
        }
    }

    // --- XLSX writers ---

    private void writeFaultsXlsx(List<FaultReport> faults, OutputStream os) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Faults");
            writeXlsxHeader(sheet, "id", "title", "description", "faultPriority",
                    "faultClassification", "locationId", "locationAddress");
            int rowIdx = 1;
            for (FaultReport f : faults) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(f.getId());
                row.createCell(1).setCellValue(f.getTitle());
                row.createCell(2).setCellValue(f.getDescription());
                row.createCell(3).setCellValue(f.getFaultPriority() != null ? f.getFaultPriority().name() : "");
                row.createCell(4).setCellValue(f.getFaultClassification() != null ? f.getFaultClassification().name() : "");
                row.createCell(5).setCellValue(f.getLocation().getId());
                row.createCell(6).setCellValue(f.getLocation().getAddress());
            }
            workbook.write(os);
        }
    }

    private void writeInterventionsXlsx(List<Intervention> interventions, OutputStream os) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Interventions");
            writeXlsxHeader(sheet, "id", "description", "faultReportId", "crewId");
            int rowIdx = 1;
            for (Intervention i : interventions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(i.getId());
                row.createCell(1).setCellValue(i.getDescription());
                row.createCell(2).setCellValue(i.getFaultReport().getId());
                row.createCell(3).setCellValue(i.getCrew().getId());
            }
            workbook.write(os);
        }
    }

    private void writeAnalyticsXlsx(Map<FaultPriority, Long> byPriority, long totalFaults,
                                     long totalInterventions, OutputStream os) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Analytics");
            writeXlsxHeader(sheet, "metric", "value");
            int rowIdx = 1;
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue("totalFaults");
            r.createCell(1).setCellValue(totalFaults);
            r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue("totalInterventions");
            r.createCell(1).setCellValue(totalInterventions);
            for (Map.Entry<FaultPriority, Long> e : byPriority.entrySet()) {
                r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue("faults_" + e.getKey().name());
                r.createCell(1).setCellValue(e.getValue());
            }
            workbook.write(os);
        }
    }

    private void writeXlsxHeader(Sheet sheet, String... headers) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }

    // --- Persistence helpers ---

    private ExportBatchResponse saveBatch(ExportRequest request, Long userId, Path filePath, BatchStatus status) {
        ExportBatch batch = new ExportBatch();
        batch.setExportType(request.type());
        batch.setExportFormat(request.format());
        batch.setExportStatus(status);
        batch.setCreatedByUserId(userId);
        batch.setCreatedAt(LocalDateTime.now());
        if (filePath != null) {
            batch.setFileName(filePath.getFileName().toString());
            batch.setFilePath(filePath.toAbsolutePath().toString());
        }
        ExportBatch saved = exportBatchRepository.save(batch);
        return mapToResponse(saved);
    }

    private ExportBatchResponse mapToResponse(ExportBatch batch) {
        String downloadUrl = batch.getExportStatus() == BatchStatus.COMPLETED
                ? "/api/v1/export/" + batch.getId() + "/download"
                : null;
        return new ExportBatchResponse(
                batch.getId(),
                batch.getExportType(),
                batch.getExportFormat(),
                batch.getExportStatus(),
                downloadUrl,
                batch.getCreatedAt());
    }

    private void validateFormat(String format) {
        if (format == null || (!format.equalsIgnoreCase("CSV") && !format.equalsIgnoreCase("XLSX"))) {
            throw new BadRequestException("Invalid format '" + format + "'. Valid values: CSV, XLSX");
        }
    }

    private boolean isCsv(ExportRequest request) {
        return "CSV".equalsIgnoreCase(request.format());
    }
}
