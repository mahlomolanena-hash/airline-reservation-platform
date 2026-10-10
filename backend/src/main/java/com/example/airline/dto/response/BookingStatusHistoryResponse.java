package com.example.airline.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BookingStatusHistoryResponse {
    private String status;
    private String notes;
    private Instant changedAt;
}
