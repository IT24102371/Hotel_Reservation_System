package com.hotel.eventreservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "venue_availability",
       uniqueConstraints = @UniqueConstraint(columnNames = {"venue_id", "date", "start_time", "end_time"}))
public class VenueAvailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "availability_id")
    private Long availabilityId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    
    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
    
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "maintenance_reason")
    private String maintenanceReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum AvailabilityStatus {
        AVAILABLE, BOOKED, MAINTENANCE, BLOCKED
    }
    
    // Constructors
    public VenueAvailability() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public VenueAvailability(Venue venue, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this();
        this.venue = venue;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AvailabilityStatus.AVAILABLE;
    }
    
    public VenueAvailability(Venue venue, LocalDate date, LocalTime startTime, LocalTime endTime, AvailabilityStatus status) {
        this();
        this.venue = venue;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getAvailabilityId() {
        return availabilityId;
    }
    
    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }
    
    public Venue getVenue() {
        return venue;
    }
    
    public void setVenue(Venue venue) {
        this.venue = venue;
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
    
    public AvailabilityStatus getStatus() {
        return status;
    }
    
    public void setStatus(AvailabilityStatus status) {
        this.status = status;
    }
    
    public Boolean getIsAvailable() {
        return status == AvailabilityStatus.AVAILABLE;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.status = isAvailable ? AvailabilityStatus.AVAILABLE : AvailabilityStatus.BOOKED;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
