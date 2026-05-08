package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.util;

import org.springframework.web.multipart.MultipartFile;

public class FileTypeUtil {

    public static boolean isCsv(MultipartFile file) {
        String name = file.getOriginalFilename();
        String ct = file.getContentType();
        return (name != null && name.toLowerCase().endsWith(".csv"))
                || "text/csv".equalsIgnoreCase(ct)
                || "application/csv".equalsIgnoreCase(ct);
    }

    public static boolean isXlsx(MultipartFile file) {
        String name = file.getOriginalFilename();
        String ct = file.getContentType();
        return (name != null && name.toLowerCase().endsWith(".xlsx"))
                || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equalsIgnoreCase(ct);
    }
}
