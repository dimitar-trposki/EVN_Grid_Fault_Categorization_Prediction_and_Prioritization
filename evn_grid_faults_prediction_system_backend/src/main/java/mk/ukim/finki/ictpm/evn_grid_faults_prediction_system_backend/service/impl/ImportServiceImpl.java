package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ImportBatchResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.*;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.*;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.*;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.ImportService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.util.FileTypeUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

// TODO: wire audit log once Module 18 is complete
@Service
@RequiredArgsConstructor
public class ImportServiceImpl implements ImportService {

    private final ImportBatchRepository importBatchRepository;
    private final FaultReportRepository faultReportRepository;
    private final FaultStatusHistoryRepository faultStatusHistoryRepository;
    private final LocationRepository locationRepository;
    private final RegionRepository regionRepository;
    private final EquipmentRepository equipmentRepository;
    private final CustomerRepository customerRepository;

    @Override
    public ImportBatchResponse importFaults(MultipartFile file, Long userId) {
        validateFile(file);
        String fileType = detectType(file);
        List<Map<String, String>> rows;
        try {
            rows = parseFile(file);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse file: " + e.getMessage());
        }

        // TODO: add streaming support for large files
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 0; i < rows.size(); i++) {
            try {
                processFaultRow(rows.get(i));
                success++;
            } catch (Exception e) {
                errors.add("Row " + (i + 2) + ": " + e.getMessage());
            }
        }

        int total = rows.size();
        BatchStatus status = (success == 0 && total > 0) ? BatchStatus.FAILED : BatchStatus.COMPLETED;
        return saveBatch(file.getOriginalFilename(), fileType, userId, total, success, errors, status);
    }

    @Override
    public ImportBatchResponse importLocations(MultipartFile file, Long userId) {
        validateFile(file);
        String fileType = detectType(file);
        List<Map<String, String>> rows;
        try {
            rows = parseFile(file);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse file: " + e.getMessage());
        }

        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 0; i < rows.size(); i++) {
            try {
                processLocationRow(rows.get(i));
                success++;
            } catch (Exception e) {
                errors.add("Row " + (i + 2) + ": " + e.getMessage());
            }
        }

        int total = rows.size();
        BatchStatus status = (success == 0 && total > 0) ? BatchStatus.FAILED : BatchStatus.COMPLETED;
        return saveBatch(file.getOriginalFilename(), fileType, userId, total, success, errors, status);
    }

    @Override
    public ImportBatchResponse importEquipment(MultipartFile file, Long userId) {
        validateFile(file);
        String fileType = detectType(file);
        List<Map<String, String>> rows;
        try {
            rows = parseFile(file);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse file: " + e.getMessage());
        }

        List<String> errors = new ArrayList<>();
        int success = 0;
        for (int i = 0; i < rows.size(); i++) {
            try {
                processEquipmentRow(rows.get(i));
                success++;
            } catch (Exception e) {
                errors.add("Row " + (i + 2) + ": " + e.getMessage());
            }
        }

        int total = rows.size();
        BatchStatus status = (success == 0 && total > 0) ? BatchStatus.FAILED : BatchStatus.COMPLETED;
        return saveBatch(file.getOriginalFilename(), fileType, userId, total, success, errors, status);
    }

    @Override
    public ImportBatchResponse getBatchStatus(Long batchId) {
        ImportBatch batch = importBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("ImportBatch", batchId));
        return mapToResponse(batch);
    }

    // --- Row processors ---

    private void processFaultRow(Map<String, String> row) {
        String title = get(row, "title");
        String description = get(row, "description");
        String locationIdStr = get(row, "locationId");

        if (isBlank(title)) throw new BadRequestException("'title' is required");
        if (title.length() > 80) throw new BadRequestException("'title' must not exceed 80 characters");
        if (isBlank(description)) throw new BadRequestException("'description' is required");
        if (isBlank(locationIdStr)) throw new BadRequestException("'locationId' is required");

        Long locationId = parseLong(locationIdStr, "locationId");
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new BadRequestException("Location not found: " + locationId));

        FaultPriority priority = FaultPriority.LOW;
        String priorityStr = get(row, "priority");
        if (!isBlank(priorityStr)) {
            try {
                priority = FaultPriority.valueOf(priorityStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid priority '" + priorityStr
                        + "'. Valid: " + Arrays.toString(FaultPriority.values()));
            }
        }

        FaultClassification classification = FaultClassification.OTHER;
        String classStr = get(row, "classification");
        if (!isBlank(classStr)) {
            try {
                classification = FaultClassification.valueOf(classStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid classification '" + classStr
                        + "'. Valid: " + Arrays.toString(FaultClassification.values()));
            }
        }

        // FaultType enum is currently empty; parse if provided, reject invalid values
        FaultType faultType = null;
        String faultTypeStr = get(row, "faultType");
        if (!isBlank(faultTypeStr)) {
            try {
                faultType = FaultType.valueOf(faultTypeStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid faultType '" + faultTypeStr + "'");
            }
        }

        Customer customer = null;
        String customerIdStr = get(row, "customerId");
        if (!isBlank(customerIdStr)) {
            Long customerId = parseLong(customerIdStr, "customerId");
            customer = customerRepository.findById(customerId).orElse(null);
        }

        FaultReport fault = new FaultReport();
        fault.setTitle(title);
        fault.setDescription(description);
        fault.setLocation(location);
        fault.setCustomer(customer);
        fault.setFaultType(faultType);
        fault.setFaultPriority(priority);
        fault.setFaultClassification(classification);
        fault.setSourceType(FaultSourceType.IMPORTED);
        FaultReport saved = faultReportRepository.save(fault);

        FaultStatusHistory history = new FaultStatusHistory();
        history.setFaultStatus(FaultStatus.REPORTED);
        history.setChangedAt(LocalDateTime.now());
        history.setFaultReport(saved);
        faultStatusHistoryRepository.save(history);
    }

    private void processLocationRow(Map<String, String> row) {
        String latStr = get(row, "latitude");
        String lonStr = get(row, "longitude");
        String address = get(row, "address");
        String regionIdStr = get(row, "regionId");

        if (isBlank(latStr)) throw new BadRequestException("'latitude' is required");
        if (isBlank(lonStr)) throw new BadRequestException("'longitude' is required");
        if (isBlank(address)) throw new BadRequestException("'address' is required");
        if (address.length() > 100) throw new BadRequestException("'address' must not exceed 100 characters");
        if (isBlank(regionIdStr)) throw new BadRequestException("'regionId' is required");

        double latitude = parseDouble(latStr, "latitude");
        double longitude = parseDouble(lonStr, "longitude");
        Long regionId = parseLong(regionIdStr, "regionId");

        Region region = regionRepository.findById(regionId)
                .orElseThrow(() -> new BadRequestException("Region not found: " + regionId));

        Location location = new Location();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAddress(address);
        location.setRegion(region);
        locationRepository.save(location);
    }

    private void processEquipmentRow(Map<String, String> row) {
        String name = get(row, "name");
        String equipmentTypeStr = get(row, "equipmentType");
        String locationIdStr = get(row, "locationId");

        if (isBlank(name)) throw new BadRequestException("'name' is required");
        if (name.length() > 80) throw new BadRequestException("'name' must not exceed 80 characters");
        if (isBlank(equipmentTypeStr)) throw new BadRequestException("'equipmentType' is required");
        if (isBlank(locationIdStr)) throw new BadRequestException("'locationId' is required");

        EquipmentType equipmentType;
        try {
            equipmentType = EquipmentType.valueOf(equipmentTypeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid equipmentType '" + equipmentTypeStr
                    + "'. Valid: " + Arrays.toString(EquipmentType.values()));
        }

        Long locationId = parseLong(locationIdStr, "locationId");
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new BadRequestException("Location not found: " + locationId));

        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setEquipmentType(equipmentType);
        equipment.setLocation(location);
        equipmentRepository.save(equipment);
    }

    // --- File parsing ---

    private List<Map<String, String>> parseFile(MultipartFile file) throws IOException {
        return FileTypeUtil.isCsv(file) ? parseCsv(file) : parseXlsx(file);
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException {
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()
                .parse(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                rows.add(record.toMap());
            }
            return rows;
        }
    }

    private List<Map<String, String>> parseXlsx(MultipartFile file) throws IOException {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Map<String, String>> rows = new ArrayList<>();

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return rows;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cellToString(cell));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    rowMap.put(headers.get(j), cellToString(row.getCell(j)));
                }
                rows.add(rowMap);
            }
            return rows;
        }
    }

    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                yield d == Math.floor(d) ? String.valueOf((long) d) : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    // --- Validation helpers ---

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        if (!FileTypeUtil.isCsv(file) && !FileTypeUtil.isXlsx(file)) {
            throw new BadRequestException("Unsupported file format. Only CSV and XLSX are accepted");
        }
    }

    private String detectType(MultipartFile file) {
        return FileTypeUtil.isCsv(file) ? "CSV" : "XLSX";
    }

    private String get(Map<String, String> row, String key) {
        return row.getOrDefault(key, "");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("'" + fieldName + "' must be a number, got: " + value);
        }
    }

    private double parseDouble(String value, String fieldName) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("'" + fieldName + "' must be a decimal number, got: " + value);
        }
    }

    // --- Batch persistence ---

    private ImportBatchResponse saveBatch(String fileName, String fileType, Long userId,
                                           int total, int success, List<String> errors, BatchStatus status) {
        ImportBatch batch = new ImportBatch();
        batch.setFileName(fileName != null ? fileName : "unknown");
        batch.setFileType(fileType);
        batch.setTotalRecords(total);
        batch.setSuccessfulRecords(success);
        batch.setFailedRecords(total - success);
        batch.setImportStatus(status);
        batch.setCreatedByUserId(userId);
        batch.setCreatedAt(LocalDateTime.now());
        batch.setErrors(errors.isEmpty() ? null : String.join("\n", errors));
        return mapToResponse(importBatchRepository.save(batch));
    }

    private ImportBatchResponse mapToResponse(ImportBatch batch) {
        List<String> errors = Collections.emptyList();
        if (batch.getErrors() != null && !batch.getErrors().isBlank()) {
            errors = List.of(batch.getErrors().split("\n"));
        }
        return new ImportBatchResponse(
                batch.getId(),
                batch.getFileName(),
                batch.getFileType(),
                batch.getTotalRecords(),
                batch.getSuccessfulRecords(),
                batch.getFailedRecords(),
                batch.getImportStatus(),
                batch.getCreatedAt(),
                errors
        );
    }
}
