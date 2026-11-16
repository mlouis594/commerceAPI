package com.mlouis594.CommerceAPI.user;

import jakarta.validation.constraints.Size;

/**
 * All fields optional; only non-null fields are applied. Username is immutable
 * because it is the subject of issued JWTs.
 */
public record UpdateUserRequest(
        @Size(min = 1, max = 50) String firstName,
        @Size(min = 1, max = 50) String lastName,
        @Size(min = 8, max = 72) String password) {
}
