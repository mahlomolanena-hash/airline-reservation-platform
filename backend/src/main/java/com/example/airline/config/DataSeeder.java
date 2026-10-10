package com.example.airline.config;

import com.example.airline.domain.*;
import com.example.airline.repository.AirportRepository;
import com.example.airline.repository.FlightRepository;
import com.example.airline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Seeds the dev/H2 database with a demo admin, a few airports, and a couple of
 * scheduled flights so the API is immediately explorable via Swagger.
 * Only active on the "dev" profile — never runs against prod.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("Admin123!"))
                .firstName("Admin")
                .lastName("User")
                .roles(Set.of(Role.ADMIN, Role.PASSENGER))
                .build();
        userRepository.save(admin);

        Airport jfk = airportRepository.save(Airport.builder()
                .code("JFK").name("John F. Kennedy International").city("New York").country("USA").build());
        Airport lax = airportRepository.save(Airport.builder()
                .code("LAX").name("Los Angeles International").city("Los Angeles").country("USA").build());
        Airport lhr = airportRepository.save(Airport.builder()
                .code("LHR").name("Heathrow Airport").city("London").country("UK").build());

        Instant now = Instant.now();

        flightRepository.save(Flight.builder()
                .flightNumber("AA100")
                .airline("American Airlines")
                .origin(jfk)
                .destination(lax)
                .departureTime(now.plus(3, ChronoUnit.DAYS))
                .arrivalTime(now.plus(3, ChronoUnit.DAYS).plus(6, ChronoUnit.HOURS))
                .totalSeats(180)
                .availableSeats(180)
                .price(new BigDecimal("249.99"))
                .status(FlightStatus.SCHEDULED)
                .build());

        flightRepository.save(Flight.builder()
                .flightNumber("BA200")
                .airline("British Airways")
                .origin(jfk)
                .destination(lhr)
                .departureTime(now.plus(5, ChronoUnit.DAYS))
                .arrivalTime(now.plus(5, ChronoUnit.DAYS).plus(7, ChronoUnit.HOURS))
                .totalSeats(220)
                .availableSeats(220)
                .price(new BigDecimal("589.00"))
                .status(FlightStatus.SCHEDULED)
                .build());
    }
}
