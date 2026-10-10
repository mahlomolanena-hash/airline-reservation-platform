package com.example.airline.controller;

import com.example.airline.dto.request.CreateBookingRequest;
import com.example.airline.dto.request.UpdateBookingStatusRequest;
import com.example.airline.dto.response.BookingResponse;
import com.example.airline.dto.response.BookingStatusHistoryResponse;
import com.example.airline.dto.response.PageResponse;
import com.example.airline.security.UserPrincipal;
import com.example.airline.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, principal.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(bookingService.getBooking(id, principal.getId(), isAdmin));
    }

    /** Look up a booking by its human-readable reference (e.g. what a passenger would type in at check-in). */
    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingResponse> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(bookingService.getByReference(reference));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<BookingStatusHistoryResponse>> getStatusHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        bookingService.getBooking(id, principal.getId(), isAdmin); // enforces ownership/admin check
        return ResponseEntity.ok(bookingService.getStatusHistory(id));
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<BookingResponse>> myBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(bookingService.getBookingsForUser(principal.getId(), pageable)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(bookingService.cancelBooking(id, principal.getId(), isAdmin));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<BookingResponse>> allBookings(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(bookingService.getAllBookings(pageable)));
    }

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        return ResponseEntity.ok(bookingService.updateStatus(id, request));
    }
}
