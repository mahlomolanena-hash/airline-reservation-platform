package com.example.airline.service;

import com.example.airline.domain.*;
import com.example.airline.dto.request.BookingPassengerRequest;
import com.example.airline.dto.request.CreateBookingRequest;
import com.example.airline.exception.InvalidBookingStateException;
import com.example.airline.exception.SeatUnavailableException;
import com.example.airline.repository.BookingRepository;
import com.example.airline.repository.BookingStatusHistoryRepository;
import com.example.airline.repository.FlightRepository;
import com.example.airline.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingStatusHistoryRepository statusHistoryRepository;
    @InjectMocks private BookingService bookingService;

    private Airport airport(String code) {
        return Airport.builder().id(1L).code(code).name(code + " Intl").city(code).country("USA").build();
    }

    private Flight sampleFlight(int availableSeats) {
        return Flight.builder()
                .id(1L).flightNumber("AA100").airline("American Airlines")
                .origin(airport("JFK")).destination(airport("LAX"))
                .departureTime(Instant.now()).arrivalTime(Instant.now())
                .totalSeats(180).availableSeats(availableSeats)
                .price(new BigDecimal("200.00"))
                .status(FlightStatus.SCHEDULED)
                .build();
    }

    private CreateBookingRequest requestWithPassengers(int count) {
        CreateBookingRequest request = new CreateBookingRequest();
        request.setFlightId(1L);
        List<BookingPassengerRequest> passengers = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            BookingPassengerRequest p = new BookingPassengerRequest();
            p.setFirstName("Jane");
            p.setLastName("Doe" + i);
            p.setPassportNumber("P123456" + i);
            passengers.add(p);
        }
        request.setPassengers(passengers);
        return request;
    }

    @Test
    void shouldCreateBookingAndReserveSeatsWhenAvailable() {
        User user = User.builder().id(1L).email("a@b.com").build();
        Flight flight = sampleFlight(5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);
        when(bookingRepository.existsByBookingReference(any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });

        var response = bookingService.createBooking(requestWithPassengers(2), 1L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("400.00"); // 200 * 2 passengers
        assertThat(flight.getAvailableSeats()).isEqualTo(3); // 5 - 2
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getPassengers()).hasSize(2);
        assertThat(response.getBookingReference()).hasSize(6);
    }

    @Test
    void shouldRejectBookingWhenInsufficientSeats() {
        User user = User.builder().id(1L).email("a@b.com").build();
        Flight flight = sampleFlight(1); // only 1 seat left

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));

        assertThatThrownBy(() -> bookingService.createBooking(requestWithPassengers(3), 1L))
                .isInstanceOf(SeatUnavailableException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void shouldCancelBookingAndReleaseSeatsBackToFlight() {
        User user = User.builder().id(1L).email("a@b.com").build();
        Flight flight = sampleFlight(178); // 2 seats already taken by this booking
        Booking booking = Booking.builder()
                .id(50L).bookingReference("ABC123").user(user).flight(flight)
                .status(BookingStatus.CONFIRMED).totalAmount(new BigDecimal("400.00"))
                .build();
        booking.addPassenger(BookingPassenger.builder().firstName("Jane").lastName("Doe0").passportNumber("P1").build());
        booking.addPassenger(BookingPassenger.builder().firstName("Jane").lastName("Doe1").passportNumber("P2").build());

        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
        when(flightRepository.save(any(Flight.class))).thenReturn(flight);

        var response = bookingService.cancelBooking(50L, 1L, false);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
        assertThat(flight.getAvailableSeats()).isEqualTo(180); // released back to full
    }

    @Test
    void shouldRejectCancellingAnAlreadyCancelledBooking() {
        User user = User.builder().id(1L).email("a@b.com").build();
        Flight flight = sampleFlight(180);
        Booking booking = Booking.builder()
                .id(50L).bookingReference("ABC123").user(user).flight(flight)
                .status(BookingStatus.CANCELLED).totalAmount(BigDecimal.TEN)
                .build();

        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(50L, 1L, false))
                .isInstanceOf(InvalidBookingStateException.class);

        verify(flightRepository, never()).save(any());
    }
}
