package com.example.airline.exception;

/** Thrown when an operation is attempted against a booking in a status that doesn't allow it
 *  (e.g. cancelling a booking that's already cancelled). */
public class InvalidBookingStateException extends RuntimeException {
    public InvalidBookingStateException(String message) {
        super(message);
    }
}
