package com.example.airline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class FlightResponse {
    private Long id;
    private String flightNumber;
    private String airline;
    private AirportResponse origin;
    private AirportResponse destination;
    private Instant departureTime;
    private Instant arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal price;
    private String status;
}
