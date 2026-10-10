package com.example.airline.controller;

import com.example.airline.dto.request.AirportRequest;
import com.example.airline.dto.response.AirportResponse;
import com.example.airline.service.AirportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@RequiredArgsConstructor
@Tag(name = "Airports")
public class AirportController {

    private final AirportService airportService;

    @GetMapping
    public ResponseEntity<List<AirportResponse>> listAll() {
        return ResponseEntity.ok(airportService.listAll());
    }

    @PostMapping
    public ResponseEntity<AirportResponse> create(@Valid @RequestBody AirportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(airportService.create(request));
    }
}
