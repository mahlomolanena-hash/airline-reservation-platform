package com.example.airline.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AirportResponse {
    private Long id;
    private String code;
    private String name;
    private String city;
    private String country;
}
