package com.example.airline.controller;

import com.example.airline.dto.request.AirportRequest;
import com.example.airline.dto.request.FlightRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end slice: real Spring context, real H2 database, real Spring Security filter chain.
 *
 * IMPORTANT: named *Test.java (not *IT.java) so Maven's default Surefire runner actually
 * executes it. A previous project's integration test used the *IT.java convention, which
 * requires the Failsafe plugin to run -- that plugin was never configured, so the test
 * silently never ran despite existing in the repo. Naming it *Test.java here means "mvn test"
 * actually exercises this real security-filter-chain behavior, not just the mocked unit tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class FlightControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void publicUserCanSearchFlights() throws Exception {
        mockMvc.perform(get("/api/v1/flights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void anonymousUserCannotCreateFlight() throws Exception {
        FlightRequest request = new FlightRequest();
        request.setFlightNumber("ZZ999");
        request.setAirline("Should Fail Airways");
        request.setOriginId(1L);
        request.setDestinationId(2L);
        request.setDepartureTime(Instant.now());
        request.setArrivalTime(Instant.now());
        request.setTotalSeats(100);
        request.setPrice(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/v1/flights")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateAirport() throws Exception {
        AirportRequest request = new AirportRequest();
        request.setCode("XYZ");
        request.setName("Test Airport");
        request.setCity("Test City");
        request.setCountry("Test Country");

        mockMvc.perform(post("/api/v1/airports")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("XYZ"));
    }

    @Test
    void invalidRegistrationReturnsValidationErrors() throws Exception {
        String badPayload = "{\"email\":\"not-an-email\",\"password\":\"short\"}";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(badPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
