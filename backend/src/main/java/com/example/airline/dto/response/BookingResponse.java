package com.example.airline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private String status;
    private BigDecimal totalAmount;
    private FlightResponse flight;
    private List<BookingPassengerResponse> passengers;
    private Instant createdAt;
}
