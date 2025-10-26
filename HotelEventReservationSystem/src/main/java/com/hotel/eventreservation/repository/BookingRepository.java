package com.hotel.eventreservation.repository;

import com.hotel.eventreservation.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByGuestUserId(Long guestId);
    
    List<Booking> findByVenueVenueId(Long venueId);
    
    List<Booking> findByBookingStatus(Booking.BookingStatus status);
    
    Optional<Booking> findByReferenceCode(String referenceCode);
    
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.guest LEFT JOIN FETCH b.venue WHERE b.referenceCode = :referenceCode")
    Optional<Booking> findByReferenceCodeWithDetails(@Param("referenceCode") String referenceCode);
    
    @Query("SELECT b FROM Booking b WHERE b.eventDate = :date AND b.bookingStatus IN ('PENDING', 'CONFIRMED')")
    List<Booking> findByEventDateAndActiveStatus(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.eventDate BETWEEN :startDate AND :endDate ORDER BY b.eventDate, b.startTime")
    List<Booking> findByEventDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT b FROM Booking b WHERE b.venue.venueId = :venueId AND b.eventDate = :date AND b.bookingStatus IN ('PENDING', 'CONFIRMED')")
    List<Booking> findByVenueAndDateAndActiveStatus(@Param("venueId") Long venueId, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.guest.email = :email ORDER BY b.createdAt DESC")
    List<Booking> findByGuestEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    @Query("SELECT b FROM Booking b WHERE b.eventType = :eventType AND b.bookingStatus = :status")
    List<Booking> findByEventTypeAndStatus(@Param("eventType") String eventType, @Param("status") Booking.BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.eventDate = :date AND b.bookingStatus IN ('PENDING', 'CONFIRMED')")
    Long countActiveBookingsByDate(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' AND b.createdAt < :deadline")
    List<Booking> findPendingBookingsBeforeDeadline(@Param("deadline") java.time.LocalDateTime deadline);
    
    List<Booking> findByAssignedCoordinatorId(Long coordinatorId);
    
    List<Booking> findByCateringPreferencesIsNotNull();
}
