package com.mlouis594.CommerceAPI.exception;

/** Request conflicts with current state (duplicate username, user with orders, ...). Maps to 409. */
public class ResourceConflict extends RuntimeException {
    public ResourceConflict(String message) {
        super(message);
    }
}
