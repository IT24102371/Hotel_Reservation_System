package com.hotel.eventreservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "decor_preferences")
public class DecorPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "decor_id")
    private Long decorId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @Size(max = 100)
    @Column(name = "theme", length = 100)
    private String theme;
    
    @Size(max = 100)
    @Column(name = "color_scheme", length = 100)
    private String colorScheme;
    
    @Column(name = "flower_arrangements", columnDefinition = "TEXT")
    private String flowerArrangements;
    
    @Column(name = "lighting_preferences", columnDefinition = "TEXT")
    private String lightingPreferences;
    
    @Column(name = "additional_decor_requests", columnDefinition = "TEXT")
    private String additionalDecorRequests;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public DecorPreferences() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public DecorPreferences(Booking booking) {
        this();
        this.booking = booking;
    }
    
    // Getters and Setters
    public Long getDecorId() {
        return decorId;
    }
    
    public void setDecorId(Long decorId) {
        this.decorId = decorId;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    public String getColorScheme() {
        return colorScheme;
    }
    
    public void setColorScheme(String colorScheme) {
        this.colorScheme = colorScheme;
    }
    
    public String getFlowerArrangements() {
        return flowerArrangements;
    }
    
    public void setFlowerArrangements(String flowerArrangements) {
        this.flowerArrangements = flowerArrangements;
    }
    
    public String getLightingPreferences() {
        return lightingPreferences;
    }
    
    public void setLightingPreferences(String lightingPreferences) {
        this.lightingPreferences = lightingPreferences;
    }
    
    public String getAdditionalDecorRequests() {
        return additionalDecorRequests;
    }
    
    public void setAdditionalDecorRequests(String additionalDecorRequests) {
        this.additionalDecorRequests = additionalDecorRequests;
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
