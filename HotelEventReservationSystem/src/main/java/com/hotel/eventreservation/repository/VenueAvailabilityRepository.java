package com.hotel.eventreservation.repository;

import com.hotel.eventreservation.model.VenueAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface VenueAvailabilityRepository extends JpaRepository<VenueAvailability, Long> {
    
    List<VenueAvailability> findByVenueVenueIdAndDateAndStatus(Long venueId, LocalDate date, VenueAvailability.AvailabilityStatus status);
    
    @Query("SELECT va FROM VenueAvailability va WHERE va.venue.venueId = :venueId AND va.date = :date AND va.status = :status AND " +
           "((va.startTime <= :startTime AND va.endTime > :startTime) OR " +
           "(va.startTime < :endTime AND va.endTime >= :endTime) OR " +
           "(va.startTime >= :startTime AND va.endTime <= :endTime))")
    List<VenueAvailability> findConflictingAvailability(@Param("venueId") Long venueId, 
                                                        @Param("date") LocalDate date,
                                                        @Param("startTime") LocalTime startTime,
                                                        @Param("endTime") LocalTime endTime,
                                                        @Param("status") VenueAvailability.AvailabilityStatus status);
    
    @Query("SELECT va FROM VenueAvailability va WHERE va.venue.venueId = :venueId AND va.date BETWEEN :startDate AND :endDate ORDER BY va.date, va.startTime")
    List<VenueAvailability> findByVenueAndDateRange(@Param("venueId") Long venueId, 
                                                    @Param("startDate") LocalDate startDate, 
                                                    @Param("endDate") LocalDate endDate);
    
    List<VenueAvailability> findByDateAndStatus(LocalDate date, VenueAvailability.AvailabilityStatus status);
    
    List<VenueAvailability> findByDate(LocalDate date);
    
    @Query("SELECT va FROM VenueAvailability va WHERE va.venue.venueType = :venueType AND va.date = :date AND va.status = :status")
    List<VenueAvailability> findByVenueTypeAndDateAndStatus(@Param("venueType") String venueType, 
                                                           @Param("date") LocalDate date,
                                                           @Param("status") VenueAvailability.AvailabilityStatus status);
    
    @Query("SELECT va FROM VenueAvailability va WHERE va.venue.venueId = :venueId AND va.date = :date AND " +
           "((va.startTime <= :startTime AND va.endTime > :startTime) OR " +
           "(va.startTime < :endTime AND va.endTime >= :endTime) OR " +
           "(va.startTime >= :startTime AND va.endTime <= :endTime))")
    List<VenueAvailability> findByVenueAndDateAndTimeRange(@Param("venueId") Long venueId, 
                                                          @Param("date") LocalDate date,
                                                          @Param("startTime") LocalTime startTime,
                                                          @Param("endTime") LocalTime endTime);
    
}
