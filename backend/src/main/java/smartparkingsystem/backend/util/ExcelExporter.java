package smartparkingsystem.backend.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import smartparkingsystem.backend.dto.response.admin.SummaryResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExcelExporter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static byte[] exportSummaryToExcel(SummaryResponse summary, LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Summary Report");

            // Set column widths
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 5000);

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Create data style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            numberStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Title row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁNG CÁO TỔNG HỢP HỆ THỐNG");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

            // Date range row
            Row dateRow = sheet.createRow(2);
            Cell dateLabel = dateRow.createCell(0);
            dateLabel.setCellValue("Khoảng thời gian:");
            dateLabel.setCellStyle(dataStyle);
            Cell dateValue = dateRow.createCell(1);
            String dateRange = startDate.format(DATE_FORMAT) + " -> " + endDate.format(DATE_FORMAT);
            dateValue.setCellValue(dateRange);
            dateValue.setCellStyle(dataStyle);

            // Export date row
            Row exportRow = sheet.createRow(3);
            Cell exportLabel = exportRow.createCell(0);
            exportLabel.setCellValue("Ngày xuất báng cáo:");
            exportLabel.setCellStyle(dataStyle);
            Cell exportValue = exportRow.createCell(1);
            exportValue.setCellValue(LocalDateTime.now().format(DATE_FORMAT));
            exportValue.setCellStyle(dataStyle);

            // Empty row
            sheet.createRow(4);

            // Headers
            Row headerRow = sheet.createRow(5);
            Cell header1 = headerRow.createCell(0);
            header1.setCellValue("Chỉ tiêu");
            header1.setCellStyle(headerStyle);
            Cell header2 = headerRow.createCell(1);
            header2.setCellValue("Giá trị");
            header2.setCellStyle(headerStyle);

            // Data rows
            int rowNum = 6;

            // Total Revenue
            Row revenueRow = sheet.createRow(rowNum++);
            Cell revenueLabel = revenueRow.createCell(0);
            revenueLabel.setCellValue("Tổng doanh thu");
            revenueLabel.setCellStyle(dataStyle);
            Cell revenueValue = revenueRow.createCell(1);
            revenueValue.setCellValue(summary.getTotalRevenue() != null ? summary.getTotalRevenue().doubleValue() : 0);
            revenueValue.setCellStyle(numberStyle);

            // Total Sessions
            Row sessionsRow = sheet.createRow(rowNum++);
            Cell sessionsLabel = sessionsRow.createCell(0);
            sessionsLabel.setCellValue("Tổng số phiên gửi xe");
            sessionsLabel.setCellStyle(dataStyle);
            Cell sessionsValue = sessionsRow.createCell(1);
            sessionsValue.setCellValue(summary.getTotalSessions());
            sessionsValue.setCellStyle(numberStyle);

            // Parked Count
            Row parkedRow = sheet.createRow(rowNum++);
            Cell parkedLabel = parkedRow.createCell(0);
            parkedLabel.setCellValue("Số xe đang gửi");
            parkedLabel.setCellStyle(dataStyle);
            Cell parkedValue = parkedRow.createCell(1);
            parkedValue.setCellValue(summary.getParkedCount());
            parkedValue.setCellStyle(numberStyle);

            // Active Subscriptions
            Row subsRow = sheet.createRow(rowNum++);
            Cell subsLabel = subsRow.createCell(0);
            subsLabel.setCellValue("Số gói đăng ký hoạt động");
            subsLabel.setCellStyle(dataStyle);
            Cell subsValue = subsRow.createCell(1);
            subsValue.setCellValue(summary.getActiveSubscriptions());
            subsValue.setCellStyle(numberStyle);

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
