package com.hotel.eventreservation.strategy;

import java.util.List;
import java.util.Map;

public interface ReportExportStrategy {
    
    /**
     * Export data in the specific format
     * @param data The data to export
     * @param filename The desired filename (without extension)
     * @return byte array of the exported file
     */
    byte[] exportData(List<Map<String, Object>> data, String filename);
    
    /**
     * Get the file extension for this export format
     * @return The file extension (e.g., ".pdf", ".csv", ".json")
     */
    String getFileExtension();
    
    /**
     * Get the MIME type for this export format
     * @return The MIME type
     */
    String getMimeType();
    
    /**
     * Get the strategy type name
     * @return The strategy type identifier
     */
    String getStrategyType();
}
