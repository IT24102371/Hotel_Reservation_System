package com.hotel.eventreservation.strategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;

public class JSONExportStrategyTest {
    
    private JSONExportStrategy jsonExportStrategy;
    
    @BeforeEach
    void setUp() {
        jsonExportStrategy = new JSONExportStrategy();
    }
    
    @Test
    void testExportDataWithLocalDateAndTime() {
        // Create test data with LocalDate, LocalTime, and LocalDateTime
        List<Map<String, Object>> testData = new ArrayList<>();
        Map<String, Object> testRecord = new HashMap<>();
        
        testRecord.put("eventDate", LocalDate.of(2024, 12, 25));
        testRecord.put("startTime", LocalTime.of(14, 30));
        testRecord.put("endTime", LocalTime.of(18, 0));
        testRecord.put("createdAt", LocalDateTime.of(2024, 10, 24, 10, 15));
        testRecord.put("eventType", "Wedding");
        testRecord.put("guestCount", 150);
        testRecord.put("status", "CONFIRMED");
        
        testData.add(testRecord);
        
        // Test that export doesn't throw an exception
        assertDoesNotThrow(() -> {
            byte[] result = jsonExportStrategy.exportData(testData, "test-export");
            assertNotNull(result);
            assertTrue(result.length > 0);
            
            // Verify the JSON contains expected content
            String jsonString = new String(result);
            assertTrue(jsonString.contains("Wedding"));
            assertTrue(jsonString.contains("2024-12-25"));
            assertTrue(jsonString.contains("14:30"));
            assertTrue(jsonString.contains("CONFIRMED"));
        });
    }
    
    @Test
    void testExportDataWithEmptyList() {
        List<Map<String, Object>> emptyData = new ArrayList<>();
        
        assertDoesNotThrow(() -> {
            byte[] result = jsonExportStrategy.exportData(emptyData, "empty-test");
            assertNotNull(result);
            assertTrue(result.length > 0);
            
            String jsonString = new String(result);
            assertEquals("[]", jsonString.trim());
        });
    }
    
    @Test
    void testExportDataWithNullValues() {
        List<Map<String, Object>> testData = new ArrayList<>();
        Map<String, Object> testRecord = new HashMap<>();
        
        testRecord.put("eventDate", LocalDate.of(2024, 12, 25));
        testRecord.put("startTime", null);
        testRecord.put("eventType", "Conference");
        testRecord.put("guestCount", null);
        
        testData.add(testRecord);
        
        assertDoesNotThrow(() -> {
            byte[] result = jsonExportStrategy.exportData(testData, "null-test");
            assertNotNull(result);
            assertTrue(result.length > 0);
        });
    }
    
    @Test
    void testGetFileExtension() {
        assertEquals(".json", jsonExportStrategy.getFileExtension());
    }
    
    @Test
    void testGetMimeType() {
        assertEquals("application/json", jsonExportStrategy.getMimeType());
    }
    
    @Test
    void testGetStrategyType() {
        assertEquals("JSON", jsonExportStrategy.getStrategyType());
    }
}
