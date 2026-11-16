package com.mlouis594.CommerceAPI.exception;

/** Maps to 404. */
public class ResourceNotFound extends RuntimeException{
    public ResourceNotFound(String message) {
        super(message);
    }
}
