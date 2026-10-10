package com.example.airline.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/** Append-only audit trail of every status transition a booking goes through. */
@Entity
@Table(name = "booking_status_history", indexes = {
        @Index(name = "idx_bsh_booking", columnList = "booking_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private String notes;

    @Column(name = "changed_at", updatable = false)
    private Instant changedAt;

    @PrePersist
    void onCreate() {
        this.changedAt = Instant.now();
    }
}
