package com.hotel.eventreservation.repository;

import com.hotel.eventreservation.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientUserUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByRecipientUserUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByRecipientUserUserIdAndIsReadTrueOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientUser.userId = :userId AND n.isRead = false")
    Long countUnreadNotificationsByUserId(@Param("userId") Long userId);
    
    List<Notification> findByAlertType(Notification.AlertType alertType);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientUser.userId = :userId AND n.alertType = :alertType ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientUserAndAlertType(@Param("userId") Long userId, @Param("alertType") Notification.AlertType alertType);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT n FROM Notification n WHERE n.recipientUser.userId = :userId AND n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientUserAndDateRange(@Param("userId") Long userId, 
                                                      @Param("startDate") LocalDateTime startDate, 
                                                      @Param("endDate") LocalDateTime endDate);
}
