package com.hotel.eventreservation.repository;

import com.hotel.eventreservation.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    
    List<Venue> findByIsActiveTrue();
    
    List<Venue> findByVenueTypeAndIsActiveTrue(Venue.VenueType venueType);
    
    @Query("SELECT v FROM Venue v WHERE v.capacity >= :minCapacity AND v.isActive = true")
    List<Venue> findByCapacityGreaterThanEqualAndIsActiveTrue(@Param("minCapacity") Integer minCapacity);
    
    @Query("SELECT v FROM Venue v WHERE v.venueName LIKE %:name% AND v.isActive = true")
    List<Venue> findByVenueNameContainingAndIsActiveTrue(@Param("name") String name);
    
    @Query("SELECT v FROM Venue v WHERE v.hourlyRate <= :maxRate AND v.isActive = true ORDER BY v.hourlyRate ASC")
    List<Venue> findByHourlyRateLessThanEqualAndIsActiveTrueOrderByHourlyRateAsc(@Param("maxRate") Double maxRate);
}
