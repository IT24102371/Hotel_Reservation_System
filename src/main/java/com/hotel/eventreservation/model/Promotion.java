package com.hotel.eventreservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
public class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Long promotionId;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "promotion_name", nullable = false, length = 100)
    private String promotionName;
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "promotion_code", nullable = false, unique = true, length = 20)
    private String promotionCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;
    
    @NotNull
    @DecimalMin(value = "0.0", message = "Discount value must be non-negative")
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @DecimalMin(value = "0.0", message = "Minimum booking amount must be non-negative")
    @Column(name = "min_booking_amount", precision = 10, scale = 2)
    private BigDecimal minBookingAmount;
    
    @NotNull
    @Future(message = "Start date must be in the future")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull
    @Future(message = "End date must be in the future")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Min(value = 1, message = "Maximum uses must be at least 1")
    @Column(name = "max_uses")
    private Integer maxUses;
    
    @Column(name = "current_uses")
    private Integer currentUses = 0;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum DiscountType {
        PERCENTAGE, FIXED
    }
    
    // Constructors
    public Promotion() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Promotion(String promotionName, String promotionCode, DiscountType discountType, 
                    BigDecimal discountValue, LocalDate startDate, LocalDate endDate) {
        this();
        this.promotionName = promotionName;
        this.promotionCode = promotionCode;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public Long getPromotionId() {
        return promotionId;
    }
    
    public void setPromotionId(Long promotionId) {
        this.promotionId = promotionId;
    }
    
    public String getPromotionName() {
        return promotionName;
    }
    
    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }
    
    public String getPromotionCode() {
        return promotionCode;
    }
    
    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }
    
    public DiscountType getDiscountType() {
        return discountType;
    }
    
    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }
    
    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }
    
    public BigDecimal getMinBookingAmount() {
        return minBookingAmount;
    }
    
    public void setMinBookingAmount(BigDecimal minBookingAmount) {
        this.minBookingAmount = minBookingAmount;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getMaxUses() {
        return maxUses;
    }
    
    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }
    
    public Integer getCurrentUses() {
        return currentUses;
    }
    
    public void setCurrentUses(Integer currentUses) {
        this.currentUses = currentUses;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    // Helper methods
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return isActive && 
               startDate.isBefore(now) && 
               endDate.isAfter(now) && 
               (maxUses == null || currentUses < maxUses);
    }
    
    public boolean canBeUsed(BigDecimal bookingAmount) {
        if (!isValid()) {
            return false;
        }
        if (minBookingAmount != null && bookingAmount.compareTo(minBookingAmount) < 0) {
            return false;
        }
        return true;
    }
    
    public BigDecimal calculateDiscount(BigDecimal bookingAmount) {
        if (!canBeUsed(bookingAmount)) {
            return BigDecimal.ZERO;
        }
        
        if (discountType == DiscountType.PERCENTAGE) {
            return bookingAmount.multiply(discountValue).divide(new BigDecimal("100"));
        } else {
            return discountValue;
        }
    }
}
