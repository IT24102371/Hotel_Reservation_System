package com.hotel.eventreservation.dto;

import com.hotel.eventreservation.model.VenueAvailability;
import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityDetailsDTO {
    private Long availabilityId;
    private Long venueId;
    private String venueName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Long bookingId;
    private String notes;
    private String maintenanceReason;
    
    public AvailabilityDetailsDTO() {}
    
    public AvailabilityDetailsDTO(VenueAvailability availability) {
        this.availabilityId = availability.getAvailabilityId();
        this.venueId = availability.getVenue().getVenueId();
        this.venueName = availability.getVenue().getVenueName();
        this.date = availability.getDate();
        this.startTime = availability.getStartTime();
        this.endTime = availability.getEndTime();
        this.status = availability.getStatus().name();
        this.bookingId = availability.getBookingId();
        this.notes = availability.getNotes();
        this.maintenanceReason = availability.getMaintenanceReason();
    }
    
    // Getters and Setters
    public Long getAvailabilityId() {
        return availabilityId;
    }
    
    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }
    
    public Long getVenueId() {
        return venueId;
    }
    
    public void setVenueId(Long venueId) {
        this.venueId = venueId;
    }
    
    public String getVenueName() {
        return venueName;
    }
    
    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getMaintenanceReason() {
        return maintenanceReason;
    }
    
    public void setMaintenanceReason(String maintenanceReason) {
        this.maintenanceReason = maintenanceReason;
    }
}
