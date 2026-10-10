package com.example.airline.repository;

import com.example.airline.domain.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AirportRepository extends JpaRepository<Airport, Long> {
    Optional<Airport> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
