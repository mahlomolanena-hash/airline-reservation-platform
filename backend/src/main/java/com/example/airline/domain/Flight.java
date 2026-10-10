package com.example.airline.domain;

import com.example.airline.exception.SeatUnavailableException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "flights", indexes = {
        @Index(name = "idx_flights_origin_dest", columnList = "origin_id, destination_id"),
        @Index(name = "idx_flights_departure", columnList = "departure_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, unique = true)
    private String flightNumber;

    @Column(nullable = false)
    private String airline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_id", nullable = false)
    private Airport origin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_id", nullable = false)
    private Airport destination;

    @Column(name = "departure_time", nullable = false)
    private Instant departureTime;

    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FlightStatus status = FlightStatus.SCHEDULED;

    // Optimistic locking guards against lost updates when two bookings race
    // for the last available seats at the same moment.
    @Version
    private Long version;

    public boolean hasAvailableSeats(int requested) {
        return availableSeats != null && availableSeats >= requested;
    }

    public void reserveSeats(int quantity) {
        if (!hasAvailableSeats(quantity)) {
            throw new SeatUnavailableException(
                    "Not enough seats on flight " + flightNumber + ": requested " + quantity + ", available " + availableSeats);
        }
        this.availableSeats -= quantity;
    }

    public void releaseSeats(int quantity) {
        this.availableSeats = Math.min(totalSeats, availableSeats + quantity);
    }
}
