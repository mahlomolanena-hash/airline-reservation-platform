package com.example.airline.repository;

import com.example.airline.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    boolean existsByBookingReference(String bookingReference);
}
