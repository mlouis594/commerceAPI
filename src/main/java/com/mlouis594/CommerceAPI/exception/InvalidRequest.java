package com.mlouis594.CommerceAPI.exception;

/** A well-formed request that breaks a business rule (e.g. checking out an empty cart). Maps to 400. */
public class InvalidRequest extends RuntimeException {
    public InvalidRequest(String message) {
        super(message);
    }
}
