package com.hotel.eventreservation.strategy;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component("csvExportStrategy")
public class CSVExportStrategy implements ReportExportStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(CSVExportStrategy.class);
    
    @Override
    public byte[] exportData(List<Map<String, Object>> data, String filename) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Report Data");
            
            if (data.isEmpty()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            Map<String, Object> firstRow = data.get(0);
            int columnIndex = 0;
            for (String key : firstRow.keySet()) {
                Cell cell = headerRow.createCell(columnIndex++);
                cell.setCellValue(key);
            }
            
            // Create data rows
            int rowIndex = 1;
            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowIndex++);
                columnIndex = 0;
                for (Object value : rowData.values()) {
                    Cell cell = row.createCell(columnIndex++);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < firstRow.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            logger.error("Error exporting data to CSV", e);
            throw new RuntimeException("Failed to export data to CSV", e);
        }
    }
    
    @Override
    public String getFileExtension() {
        return ".xlsx";
    }
    
    @Override
    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
    
    @Override
    public String getStrategyType() {
        return "CSV";
    }
}
