package com.example.airline.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateBookingRequest {

    @NotNull(message = "Flight id is required")
    private Long flightId;

    @NotEmpty(message = "At least one passenger is required")
    private List<@Valid BookingPassengerRequest> passengers;
}
