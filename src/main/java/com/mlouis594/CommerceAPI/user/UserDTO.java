package com.mlouis594.CommerceAPI.user;

import java.util.UUID;

public record UserDTO(UUID id,
                      String username,
                      String firstName,
                      String lastName) {
}
