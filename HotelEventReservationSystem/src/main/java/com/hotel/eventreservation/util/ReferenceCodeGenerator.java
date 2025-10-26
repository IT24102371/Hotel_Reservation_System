package com.hotel.eventreservation.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class ReferenceCodeGenerator {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int RANDOM_LENGTH = 6;
    private final Random random = new Random();
    
    /**
     * Generate a unique reference code for booking
     * Format: YYYYMMDD-HHMMSS-XXXXXX
     * @return Unique reference code
     */
    public String generateReferenceCode() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
        
        String datePart = now.format(dateFormatter);
        String timePart = now.format(timeFormatter);
        String randomPart = generateRandomString();
        
        return datePart + "-" + timePart + "-" + randomPart;
    }
    
    /**
     * Generate random string of specified length
     * @return Random string
     */
    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
    
    /**
     * Validate reference code format
     * @param referenceCode The reference code to validate
     * @return true if valid format, false otherwise
     */
    public boolean isValidReferenceCode(String referenceCode) {
        if (referenceCode == null || referenceCode.trim().isEmpty()) {
            return false;
        }
        
        // Format: YYYYMMDD-HHMMSS-XXXXXX
        String pattern = "\\d{8}-\\d{6}-[A-Z0-9]{6}";
        return referenceCode.matches(pattern);
    }
}
