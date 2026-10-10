package com.example.airline.service;

import com.example.airline.domain.Airport;
import com.example.airline.dto.request.AirportRequest;
import com.example.airline.dto.response.AirportResponse;
import com.example.airline.exception.DuplicateResourceException;
import com.example.airline.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;

    @Transactional(readOnly = true)
    public List<AirportResponse> listAll() {
        return airportRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AirportResponse create(AirportRequest request) {
        if (airportRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new DuplicateResourceException("An airport with code '" + request.getCode() + "' already exists");
        }

        Airport airport = Airport.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .city(request.getCity())
                .country(request.getCountry())
                .build();

        return toResponse(airportRepository.save(airport));
    }

    private AirportResponse toResponse(Airport a) {
        return AirportResponse.builder()
                .id(a.getId())
                .code(a.getCode())
                .name(a.getName())
                .city(a.getCity())
                .country(a.getCountry())
                .build();
    }
}
