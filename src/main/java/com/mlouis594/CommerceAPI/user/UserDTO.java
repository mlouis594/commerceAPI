package com.mlouis594.CommerceAPI.user;

import java.util.UUID;

public record UserDTO(UUID id,
                      String username,
                      String firstName,
                      String lastName,
                      String role) {

    public static UserDTO from(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getRole());
    }
}
