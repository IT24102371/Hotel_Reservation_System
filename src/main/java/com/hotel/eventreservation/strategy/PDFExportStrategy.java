package com.hotel.eventreservation.strategy;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component("pdfExportStrategy")
public class PDFExportStrategy implements ReportExportStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(PDFExportStrategy.class);
    
    @Override
    public byte[] exportData(List<Map<String, Object>> data, String filename) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Add title
            Paragraph title = new Paragraph("Hotel Event Reservation Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold();
            document.add(title);
            
            // Add empty line
            document.add(new Paragraph("\n"));
            
            if (data.isEmpty()) {
                document.add(new Paragraph("No data available for the selected criteria."));
            } else {
                // Create table
                Map<String, Object> firstRow = data.get(0);
                Table table = new Table(firstRow.size());
                
                // Add header row
                for (String key : firstRow.keySet()) {
                    Cell cell = new Cell().add(new Paragraph(key).setBold());
                    table.addCell(cell);
                }
                
                // Add data rows
                for (Map<String, Object> rowData : data) {
                    for (Object value : rowData.values()) {
                        Cell cell = new Cell().add(new Paragraph(value != null ? value.toString() : ""));
                        table.addCell(cell);
                    }
                }
                
                document.add(table);
            }
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            logger.error("Error exporting data to PDF", e);
            throw new RuntimeException("Failed to export data to PDF", e);
        }
    }
    
    @Override
    public String getFileExtension() {
        return ".pdf";
    }
    
    @Override
    public String getMimeType() {
        return "application/pdf";
    }
    
    @Override
    public String getStrategyType() {
        return "PDF";
    }
}
