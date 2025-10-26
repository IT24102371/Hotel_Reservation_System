package com.hotel.eventreservation.service;

import com.hotel.eventreservation.model.Booking;
import com.hotel.eventreservation.repository.BookingRepository;
import com.hotel.eventreservation.strategy.ReportExportStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private Map<String, ReportExportStrategy> reportExportStrategies;
    
    /**
     * Generate booking analytics report
     */
    public List<Map<String, Object>> generateBookingAnalytics(LocalDate startDate, LocalDate endDate, 
                                                             String eventType, Booking.BookingStatus status) {
        List<Booking> bookings;
        
        if (eventType != null && status != null) {
            bookings = bookingRepository.findByEventTypeAndStatus(eventType, status);
        } else if (eventType != null) {
            bookings = bookingRepository.findByEventTypeAndStatus(eventType, Booking.BookingStatus.CONFIRMED);
        } else if (status != null) {
            bookings = bookingRepository.findByBookingStatus(status);
        } else {
            bookings = bookingRepository.findByEventDateBetween(startDate, endDate);
            
            // If no bookings found in date range, try to get all bookings to show some data
            if (bookings.isEmpty()) {
                logger.info("No bookings found in date range {}-{}, trying to get all bookings", startDate, endDate);
                bookings = bookingRepository.findAll();
            }
        }
        
        logger.info("Found {} bookings for analytics", bookings.size());
        return convertBookingsToReportData(bookings);
    }
    
    /**
     * Generate venue utilization report
     */
    public List<Map<String, Object>> generateVenueUtilizationReport(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByEventDateBetween(startDate, endDate);
        
        // If no bookings found in date range, try to get all bookings
        if (bookings.isEmpty()) {
            logger.info("No bookings found in date range {}-{} for venue utilization, trying to get all bookings", startDate, endDate);
            bookings = bookingRepository.findAll();
        }
        
        logger.info("Found {} bookings for venue utilization", bookings.size());
        return convertBookingsToVenueReportData(bookings);
    }
    
    /**
     * Generate revenue report
     */
    public List<Map<String, Object>> generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByEventDateBetween(startDate, endDate);
        
        // If no bookings found in date range, try to get all bookings
        if (bookings.isEmpty()) {
            logger.info("No bookings found in date range {}-{} for revenue report, trying to get all bookings", startDate, endDate);
            bookings = bookingRepository.findAll();
        }
        
        logger.info("Found {} bookings for revenue report", bookings.size());
        return convertBookingsToRevenueReportData(bookings);
    }
    
    /**
     * Generate event type trends report
     */
    public List<Map<String, Object>> generateEventTypeTrendsReport(LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByEventDateBetween(startDate, endDate);
        
        // If no bookings found in date range, try to get all bookings
        if (bookings.isEmpty()) {
            logger.info("No bookings found in date range {}-{} for event type trends, trying to get all bookings", startDate, endDate);
            bookings = bookingRepository.findAll();
        }
        
        logger.info("Found {} bookings for event type trends", bookings.size());
        return convertBookingsToEventTypeReportData(bookings);
    }
    
    /**
     * Export report using specified strategy
     */
    public byte[] exportReport(String strategyType, List<Map<String, Object>> data, String filename) {
        if (strategyType == null || strategyType.trim().isEmpty()) {
            throw new IllegalArgumentException("Strategy type cannot be null or empty");
        }
        
        if (data == null) {
            data = java.util.Collections.emptyList();
        }
        
        if (filename == null || filename.trim().isEmpty()) {
            filename = "report";
        }
        
        String strategyKey = strategyType.toLowerCase() + "ExportStrategy";
        ReportExportStrategy strategy = reportExportStrategies.get(strategyKey);
        
        if (strategy == null) {
            logger.error("Export strategy not found: {} (looking for key: {})", strategyType, strategyKey);
            logger.info("Available strategies: {}", reportExportStrategies.keySet());
            throw new RuntimeException("Export strategy not found: " + strategyType);
        }
        
        try {
            return strategy.exportData(data, filename);
        } catch (Exception e) {
            logger.error("Error exporting data using strategy: {}", strategyType, e);
            throw new RuntimeException("Failed to export data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get available export formats
     */
    public Map<String, String> getAvailableExportFormats() {
        Map<String, String> formats = new HashMap<>();
        for (ReportExportStrategy strategy : reportExportStrategies.values()) {
            formats.put(strategy.getStrategyType(), strategy.getFileExtension());
        }
        return formats;
    }
    
    /**
     * Convert bookings to report data format
     */
    private List<Map<String, Object>> convertBookingsToReportData(List<Booking> bookings) {
        return bookings.stream().map(booking -> {
            Map<String, Object> data = new HashMap<>();
            data.put("Booking ID", booking.getBookingId());
            data.put("Reference Code", booking.getReferenceCode());
            data.put("Guest Name", booking.getGuest().getFullName());
            data.put("Event Type", booking.getEventType());
            data.put("Event Date", booking.getEventDate());
            data.put("Start Time", booking.getStartTime());
            data.put("End Time", booking.getEndTime());
            data.put("Guest Count", booking.getGuestCount());
            data.put("Venue", booking.getVenue().getVenueName());
            data.put("Total Cost", booking.getTotalCost());
            data.put("Status", booking.getBookingStatus());
            data.put("Created At", booking.getCreatedAt());
            return data;
        }).toList();
    }
    
    /**
     * Convert bookings to venue report data format
     */
    private List<Map<String, Object>> convertBookingsToVenueReportData(List<Booking> bookings) {
        return bookings.stream().map(booking -> {
            Map<String, Object> data = new HashMap<>();
            data.put("Venue Name", booking.getVenue().getVenueName());
            data.put("Venue Type", booking.getVenue().getVenueType());
            data.put("Capacity", booking.getVenue().getCapacity());
            data.put("Event Date", booking.getEventDate());
            data.put("Event Type", booking.getEventType());
            data.put("Guest Count", booking.getGuestCount());
            data.put("Utilization %", calculateUtilization(booking.getGuestCount(), booking.getVenue().getCapacity()));
            data.put("Revenue", booking.getTotalCost());
            return data;
        }).toList();
    }
    
    /**
     * Convert bookings to revenue report data format
     */
    private List<Map<String, Object>> convertBookingsToRevenueReportData(List<Booking> bookings) {
        return bookings.stream().map(booking -> {
            Map<String, Object> data = new HashMap<>();
            data.put("Event Date", booking.getEventDate());
            data.put("Event Type", booking.getEventType());
            data.put("Venue", booking.getVenue().getVenueName());
            data.put("Guest Count", booking.getGuestCount());
            data.put("Revenue", booking.getTotalCost());
            data.put("Status", booking.getBookingStatus());
            return data;
        }).toList();
    }
    
    /**
     * Convert bookings to event type trends report data format
     */
    private List<Map<String, Object>> convertBookingsToEventTypeReportData(List<Booking> bookings) {
        return bookings.stream().map(booking -> {
            Map<String, Object> data = new HashMap<>();
            data.put("Event Type", booking.getEventType());
            data.put("Event Date", booking.getEventDate());
            data.put("Guest Count", booking.getGuestCount());
            data.put("Revenue", booking.getTotalCost());
            data.put("Venue Type", booking.getVenue().getVenueType());
            return data;
        }).toList();
    }
    
    /**
     * Calculate venue utilization percentage
     */
    private double calculateUtilization(Integer guestCount, Integer capacity) {
        if (capacity == 0) return 0.0;
        return (double) guestCount / capacity * 100;
    }
}
