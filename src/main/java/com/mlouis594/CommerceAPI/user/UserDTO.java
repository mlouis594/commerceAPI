package com.mlouis594.CommerceAPI.user;

import java.util.UUID;

public record UserDTO(UUID id,
                      String userName,
                      String firstName,
                      String lastName) {
}
