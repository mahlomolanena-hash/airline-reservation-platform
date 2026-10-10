package com.example.airline.service;

import com.example.airline.domain.Airport;
import com.example.airline.domain.Flight;
import com.example.airline.dto.request.FlightRequest;
import com.example.airline.dto.response.AirportResponse;
import com.example.airline.dto.response.FlightResponse;
import com.example.airline.exception.DuplicateResourceException;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.AirportRepository;
import com.example.airline.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    /** departureDate, if provided, matches any flight departing on that calendar day (UTC). */
    @Transactional(readOnly = true)
    public Page<FlightResponse> search(Long originId, Long destinationId, LocalDate departureDate, Pageable pageable) {
        Instant from = departureDate != null ? departureDate.atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        Instant to = departureDate != null ? departureDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() : null;
        return flightRepository.search(originId, destinationId, from, to, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FlightResponse getById(Long id) {
        return toResponse(findFlightOrThrow(id));
    }

    @Transactional
    public FlightResponse create(FlightRequest request) {
        if (flightRepository.existsByFlightNumber(request.getFlightNumber())) {
            throw new DuplicateResourceException("A flight with number '" + request.getFlightNumber() + "' already exists");
        }

        Airport origin = airportRepository.findById(request.getOriginId())
                .orElseThrow(() -> new ResourceNotFoundException("Origin airport not found: " + request.getOriginId()));
        Airport destination = airportRepository.findById(request.getDestinationId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination airport not found: " + request.getDestinationId()));

        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .airline(request.getAirline())
                .origin(origin)
                .destination(destination)
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .price(request.getPrice())
                .build();

        return toResponse(flightRepository.save(flight));
    }

    Flight findFlightOrThrow(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));
    }

    FlightResponse toResponse(Flight f) {
        return FlightResponse.builder()
                .id(f.getId())
                .flightNumber(f.getFlightNumber())
                .airline(f.getAirline())
                .origin(toAirportResponse(f.getOrigin()))
                .destination(toAirportResponse(f.getDestination()))
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .totalSeats(f.getTotalSeats())
                .availableSeats(f.getAvailableSeats())
                .price(f.getPrice())
                .status(f.getStatus().name())
                .build();
    }

    private AirportResponse toAirportResponse(Airport a) {
        return AirportResponse.builder()
                .id(a.getId())
                .code(a.getCode())
                .name(a.getName())
                .city(a.getCity())
                .country(a.getCountry())
                .build();
    }
}
