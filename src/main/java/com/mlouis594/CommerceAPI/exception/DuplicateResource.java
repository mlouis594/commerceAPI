package com.mlouis594.CommerceAPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResource extends RuntimeException {
    public DuplicateResource(String message) {
        super(message);
    }
}
