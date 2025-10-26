package com.hotel.eventreservation.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component("jsonExportStrategy")
public class JSONExportStrategy implements ReportExportStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(JSONExportStrategy.class);
    private final ObjectMapper objectMapper;
    
    public JSONExportStrategy() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    @Override
    public byte[] exportData(List<Map<String, Object>> data, String filename) {
        try {
            logger.debug("Starting JSON export for {} records", data != null ? data.size() : 0);
            String jsonString = objectMapper.writeValueAsString(data);
            logger.debug("Successfully serialized data to JSON, length: {} characters", jsonString.length());
            return jsonString.getBytes("UTF-8");
        } catch (IOException e) {
            logger.error("Error exporting data to JSON. Data size: {}, Error: {}", 
                        data != null ? data.size() : 0, e.getMessage(), e);
            throw new RuntimeException("Failed to export data to JSON: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during JSON export: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during JSON export: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getFileExtension() {
        return ".json";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
    
    @Override
    public String getStrategyType() {
        return "JSON";
    }
}
