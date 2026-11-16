package com.mlouis594.CommerceAPI.exception;

/** Requested quantity exceeds available inventory. Maps to 409. */
public class InsufficientStock extends RuntimeException {
    public InsufficientStock(String message) {
        super(message);
    }
}
