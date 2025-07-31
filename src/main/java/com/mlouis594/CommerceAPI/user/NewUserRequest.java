package com.mlouis594.CommerceAPI.user;

public record NewUserRequest(String username,
                             String firstName,
                             String lastName,
                             String password) {
}
