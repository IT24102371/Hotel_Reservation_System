package com.hotel.eventreservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type")
    private SenderType senderType = SenderType.SYSTEM;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id")
    private User senderUser;
    
    @NotBlank
    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    // Enums
    public enum SenderType {
        SYSTEM, STAFF
    }
    
    public enum AlertType {
        GUEST_ARRIVAL, BOOKING_CHANGE, COORDINATION_ALERT, PAYMENT_REMINDER, EVENT_REMINDER,
        BOOKING_CONFIRMATION, BOOKING_CANCELLATION, SETUP_COMPLETE, CATERING_CONFIRMED
    }
    
    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Notification(User recipientUser, String messageContent, AlertType alertType) {
        this();
        this.recipientUser = recipientUser;
        this.messageContent = messageContent;
        this.alertType = alertType;
    }
    
    public Notification(User recipientUser, User senderUser, String messageContent, AlertType alertType) {
        this(recipientUser, messageContent, alertType);
        this.senderUser = senderUser;
        this.senderType = SenderType.STAFF;
    }
    
    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
    
    public User getRecipientUser() {
        return recipientUser;
    }
    
    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }
    
    public SenderType getSenderType() {
        return senderType;
    }
    
    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }
    
    public User getSenderUser() {
        return senderUser;
    }
    
    public void setSenderUser(User senderUser) {
        this.senderUser = senderUser;
    }
    
    public String getMessageContent() {
        return messageContent;
    }
    
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
    
    public AlertType getAlertType() {
        return alertType;
    }
    
    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
        if (isRead && this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getReadAt() {
        return readAt;
    }
    
    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
    
    // Helper methods
    public void markAsRead() {
        setIsRead(true);
    }
    
    public boolean isRead() {
        return isRead != null && isRead;
    }
    
    public String getSenderName() {
        if (senderType == SenderType.SYSTEM) {
            return "System";
        } else if (senderUser != null) {
            return senderUser.getFullName();
        }
        return "Unknown";
    }
}
