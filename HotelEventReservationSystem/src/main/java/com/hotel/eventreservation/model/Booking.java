package com.hotel.eventreservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    
    @NotNull
    @Future(message = "Event date must be in the future")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
    
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Min(value = 1, message = "Guest count must be at least 1")
    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;
    
    @NotNull
    @DecimalMin(value = "0.0", message = "Total cost must be non-negative")
    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status")
    private BookingStatus bookingStatus = BookingStatus.PENDING;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "reference_code", nullable = false, unique = true, length = 50)
    private String referenceCode;
    
    @Column(name = "qr_code_path", columnDefinition = "TEXT")
    private String qrCodePath;
    
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private DecorPreferences decorPreferences;
    
    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CateringPreferences cateringPreferences;
    
    @Column(name = "assigned_coordinator_id")
    private Long assignedCoordinatorId;
    
    @Column(name = "coordinator_notes", columnDefinition = "TEXT")
    private String coordinatorNotes;
    
    @Column(name = "setup_status", length = 50)
    private String setupStatus;
    
    @Column(name = "catering_notes", columnDefinition = "TEXT")
    private String cateringNotes;
    
    @Column(name = "catering_status", length = 50)
    private String cateringStatus;
    
    // Enums
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }
    
    // Constructors
    public Booking() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Booking(User guest, Venue venue, String eventType, LocalDate eventDate, 
                   LocalTime startTime, LocalTime endTime, Integer guestCount, BigDecimal totalCost) {
        this();
        this.guest = guest;
        this.venue = venue;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guestCount = guestCount;
        this.totalCost = totalCost;
    }
    
    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public User getGuest() {
        return guest;
    }
    
    public void setGuest(User guest) {
        this.guest = guest;
    }
    
    public Venue getVenue() {
        return venue;
    }
    
    public void setVenue(Venue venue) {
        this.venue = venue;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public LocalDate getEventDate() {
        return eventDate;
    }
    
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
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
    
    public Integer getGuestCount() {
        return guestCount;
    }
    
    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }
    
    public BigDecimal getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
    
    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }
    
    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
    
    public String getReferenceCode() {
        return referenceCode;
    }
    
    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }
    
    public String getQrCodePath() {
        return qrCodePath;
    }
    
    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }
    
    public String getSpecialRequests() {
        return specialRequests;
    }
    
    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
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
    
    public DecorPreferences getDecorPreferences() {
        return decorPreferences;
    }
    
    public void setDecorPreferences(DecorPreferences decorPreferences) {
        this.decorPreferences = decorPreferences;
    }
    
    public CateringPreferences getCateringPreferences() {
        return cateringPreferences;
    }
    
    public void setCateringPreferences(CateringPreferences cateringPreferences) {
        this.cateringPreferences = cateringPreferences;
    }
    
    public Long getAssignedCoordinatorId() {
        return assignedCoordinatorId;
    }
    
    public void setAssignedCoordinatorId(Long assignedCoordinatorId) {
        this.assignedCoordinatorId = assignedCoordinatorId;
    }
    
    public String getCoordinatorNotes() {
        return coordinatorNotes;
    }
    
    public void setCoordinatorNotes(String coordinatorNotes) {
        this.coordinatorNotes = coordinatorNotes;
    }
    
    public String getSetupStatus() {
        return setupStatus;
    }
    
    public void setSetupStatus(String setupStatus) {
        this.setupStatus = setupStatus;
    }
    
    public String getCateringNotes() {
        return cateringNotes;
    }
    
    public void setCateringNotes(String cateringNotes) {
        this.cateringNotes = cateringNotes;
    }
    
    public String getCateringStatus() {
        return cateringStatus;
    }
    
    public void setCateringStatus(String cateringStatus) {
        this.cateringStatus = cateringStatus;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
