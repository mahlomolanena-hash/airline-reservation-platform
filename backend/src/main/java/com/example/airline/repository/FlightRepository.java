package com.example.airline.repository;

import com.example.airline.domain.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    boolean existsByFlightNumber(String flightNumber);

    @Query("""
           select f from Flight f
           where f.status = 'SCHEDULED'
             and (:originId is null or f.origin.id = :originId)
             and (:destinationId is null or f.destination.id = :destinationId)
             and (:departureFrom is null or f.departureTime >= :departureFrom)
             and (:departureTo is null or f.departureTime < :departureTo)
           order by f.departureTime asc
           """)
    Page<Flight> search(
            @Param("originId") Long originId,
            @Param("destinationId") Long destinationId,
            @Param("departureFrom") Instant departureFrom,
            @Param("departureTo") Instant departureTo,
            Pageable pageable);
}
