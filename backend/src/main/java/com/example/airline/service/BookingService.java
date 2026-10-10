package com.example.airline.service;

import com.example.airline.domain.*;
import com.example.airline.dto.request.BookingPassengerRequest;
import com.example.airline.dto.request.CreateBookingRequest;
import com.example.airline.dto.request.UpdateBookingStatusRequest;
import com.example.airline.dto.response.*;
import com.example.airline.exception.BadRequestException;
import com.example.airline.exception.InvalidBookingStateException;
import com.example.airline.exception.ResourceNotFoundException;
import com.example.airline.repository.BookingRepository;
import com.example.airline.repository.BookingStatusHistoryRepository;
import com.example.airline.repository.FlightRepository;
import com.example.airline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;

/**
 * Owns the booking lifecycle: create (validate + reserve seats atomically), confirm,
 * cancel (release seats back). Seat reservation is guarded by optimistic locking via
 * @Version on Flight, so two passengers racing for the last seat can't both win it.
 *
 * Every status transition is appended to BookingStatusHistory for a full audit trail.
 *
 * All read methods are explicitly @Transactional(readOnly = true) so that lazy
 * associations (flight.origin, flight.destination, booking.passengers) can still be
 * read while mapping to response DTOs -- omitting this causes a
 * LazyInitializationException once the database session used for the query closes.
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String REFERENCE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O/1/I
    private final SecureRandom random = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + request.getFlightId()));

        if (flight.getStatus() != FlightStatus.SCHEDULED) {
            throw new BadRequestException("Flight " + flight.getFlightNumber() + " is not open for booking (status: " + flight.getStatus() + ")");
        }

        int passengerCount = request.getPassengers().size();

        // Throws SeatUnavailableException if not enough seats; @Version on Flight
        // makes concurrent reservations safe (optimistic locking).
        flight.reserveSeats(passengerCount);
        flightRepository.save(flight);

        Booking booking = Booking.builder()
                .bookingReference(generateUniqueReference())
                .user(user)
                .flight(flight)
                .status(BookingStatus.PENDING)
                .build();

        for (BookingPassengerRequest p : request.getPassengers()) {
            booking.addPassenger(BookingPassenger.builder()
                    .firstName(p.getFirstName())
                    .lastName(p.getLastName())
                    .passportNumber(p.getPassportNumber())
                    .dateOfBirth(p.getDateOfBirth())
                    .build());
        }

        BigDecimal total = flight.getPrice().multiply(BigDecimal.valueOf(passengerCount));
        booking.setTotalAmount(total);
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking saved = bookingRepository.save(booking);
        recordStatus(saved, BookingStatus.CONFIRMED, "Booking created and seats reserved");

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, Long requestingUserId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!isAdmin && !booking.getUser().getId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Booking not found: " + bookingId); // don't leak existence
        }

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + reference));
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsForUser(Long userId, Pageable pageable) {
        return bookingRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<BookingStatusHistoryResponse> getStatusHistory(Long bookingId) {
        return statusHistoryRepository.findByBookingIdOrderByChangedAtAsc(bookingId).stream()
                .map(h -> BookingStatusHistoryResponse.builder()
                        .status(h.getStatus().name())
                        .notes(h.getNotes())
                        .changedAt(h.getChangedAt())
                        .build())
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long requestingUserId, boolean isAdmin) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!isAdmin && !booking.getUser().getId().equals(requestingUserId)) {
            throw new ResourceNotFoundException("Booking not found: " + bookingId);
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking " + booking.getBookingReference() + " is already cancelled");
        }

        Flight flight = booking.getFlight();
        flight.releaseSeats(booking.getPassengerCount());
        flightRepository.save(flight);

        booking.setStatus(BookingStatus.CANCELLED);
        recordStatus(booking, BookingStatus.CANCELLED, "Booking cancelled, seats released back to flight");

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse updateStatus(Long bookingId, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        booking.setStatus(request.getStatus());
        recordStatus(booking, request.getStatus(), "Status updated by admin");
        return toResponse(booking);
    }

    private void recordStatus(Booking booking, BookingStatus status, String notes) {
        statusHistoryRepository.save(BookingStatusHistory.builder()
                .booking(booking)
                .status(status)
                .notes(notes)
                .build());
    }

    private String generateUniqueReference() {
        String candidate;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(REFERENCE_CHARS.charAt(random.nextInt(REFERENCE_CHARS.length())));
            }
            candidate = sb.toString();
        } while (bookingRepository.existsByBookingReference(candidate));
        return candidate;
    }

    private BookingResponse toResponse(Booking booking) {
        Flight f = booking.getFlight();
        FlightResponse flightResponse = FlightResponse.builder()
                .id(f.getId())
                .flightNumber(f.getFlightNumber())
                .airline(f.getAirline())
                .origin(AirportResponse.builder()
                        .id(f.getOrigin().getId()).code(f.getOrigin().getCode())
                        .name(f.getOrigin().getName()).city(f.getOrigin().getCity())
                        .country(f.getOrigin().getCountry()).build())
                .destination(AirportResponse.builder()
                        .id(f.getDestination().getId()).code(f.getDestination().getCode())
                        .name(f.getDestination().getName()).city(f.getDestination().getCity())
                        .country(f.getDestination().getCountry()).build())
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .totalSeats(f.getTotalSeats())
                .availableSeats(f.getAvailableSeats())
                .price(f.getPrice())
                .status(f.getStatus().name())
                .build();

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .status(booking.getStatus().name())
                .totalAmount(booking.getTotalAmount())
                .flight(flightResponse)
                .passengers(booking.getPassengers().stream().map(p -> BookingPassengerResponse.builder()
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .passportNumber(p.getPassportNumber())
                        .dateOfBirth(p.getDateOfBirth())
                        .seatNumber(p.getSeatNumber())
                        .build()).toList())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
