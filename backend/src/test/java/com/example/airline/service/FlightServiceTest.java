package com.example.airline.service;

import com.example.airline.domain.Airport;
import com.example.airline.domain.Flight;
import com.example.airline.dto.request.FlightRequest;
import com.example.airline.dto.response.FlightResponse;
import com.example.airline.exception.DuplicateResourceException;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.AirportRepository;
import com.example.airline.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock private FlightRepository flightRepository;
    @Mock private AirportRepository airportRepository;
    @InjectMocks private FlightService flightService;

    @Test
    void shouldCreateFlightWhenFlightNumberIsUnique() {
        Airport jfk = Airport.builder().id(1L).code("JFK").name("JFK Intl").city("New York").country("USA").build();
        Airport lax = Airport.builder().id(2L).code("LAX").name("LAX Intl").city("Los Angeles").country("USA").build();

        FlightRequest request = new FlightRequest();
        request.setFlightNumber("AA100");
        request.setAirline("American Airlines");
        request.setOriginId(1L);
        request.setDestinationId(2L);
        request.setDepartureTime(Instant.now().plusSeconds(3600));
        request.setArrivalTime(Instant.now().plusSeconds(3600 * 6));
        request.setTotalSeats(180);
        request.setPrice(new BigDecimal("249.99"));

        when(flightRepository.existsByFlightNumber("AA100")).thenReturn(false);
        when(airportRepository.findById(1L)).thenReturn(Optional.of(jfk));
        when(airportRepository.findById(2L)).thenReturn(Optional.of(lax));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> {
            Flight f = inv.getArgument(0);
            f.setId(10L);
            return f;
        });

        FlightResponse response = flightService.create(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getFlightNumber()).isEqualTo("AA100");
        assertThat(response.getAvailableSeats()).isEqualTo(180); // starts equal to totalSeats
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void shouldRejectDuplicateFlightNumber() {
        FlightRequest request = new FlightRequest();
        request.setFlightNumber("AA100");
        request.setAirline("American Airlines");
        request.setOriginId(1L);
        request.setDestinationId(2L);
        request.setDepartureTime(Instant.now());
        request.setArrivalTime(Instant.now());
        request.setTotalSeats(100);
        request.setPrice(BigDecimal.TEN);

        when(flightRepository.existsByFlightNumber("AA100")).thenReturn(true);

        assertThatThrownBy(() -> flightService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("AA100");

        verify(flightRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenFlightNotFound() {
        when(flightRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
