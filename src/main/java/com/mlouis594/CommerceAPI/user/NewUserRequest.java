package com.mlouis594.CommerceAPI.user;

public record NewUserRequest(String userName,
                             String firstName,
                             String lastName,
                             String password) {
}
