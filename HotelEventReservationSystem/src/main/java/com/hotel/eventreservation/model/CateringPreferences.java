package com.hotel.eventreservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "catering_preferences")
public class CateringPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "catering_id")
    private Long cateringId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;
    
    @Size(max = 100)
    @Column(name = "cuisine_type", length = 100)
    private String cuisineType;
    
    @Column(name = "dietary_restrictions", columnDefinition = "TEXT")
    private String dietaryRestrictions;
    
    @Column(name = "special_dishes", columnDefinition = "TEXT")
    private String specialDishes;
    
    @Column(name = "beverage_preferences", columnDefinition = "TEXT")
    private String beveragePreferences;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "serving_style")
    private ServingStyle servingStyle = ServingStyle.BUFFET;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum MealType {
        BREAKFAST, LUNCH, DINNER, SNACKS, COCKTAILS
    }
    
    public enum ServingStyle {
        BUFFET, PLATED, FAMILY_STYLE, COCKTAIL
    }
    
    // Constructors
    public CateringPreferences() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public CateringPreferences(Booking booking, MealType mealType) {
        this();
        this.booking = booking;
        this.mealType = mealType;
    }
    
    // Getters and Setters
    public Long getCateringId() {
        return cateringId;
    }
    
    public void setCateringId(Long cateringId) {
        this.cateringId = cateringId;
    }
    
    public Booking getBooking() {
        return booking;
    }
    
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    
    public MealType getMealType() {
        return mealType;
    }
    
    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }
    
    public String getCuisineType() {
        return cuisineType;
    }
    
    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }
    
    public String getDietaryRestrictions() {
        return dietaryRestrictions;
    }
    
    public void setDietaryRestrictions(String dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }
    
    public String getSpecialDishes() {
        return specialDishes;
    }
    
    public void setSpecialDishes(String specialDishes) {
        this.specialDishes = specialDishes;
    }
    
    public String getBeveragePreferences() {
        return beveragePreferences;
    }
    
    public void setBeveragePreferences(String beveragePreferences) {
        this.beveragePreferences = beveragePreferences;
    }
    
    public ServingStyle getServingStyle() {
        return servingStyle;
    }
    
    public void setServingStyle(ServingStyle servingStyle) {
        this.servingStyle = servingStyle;
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
