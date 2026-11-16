package com.mlouis594.CommerceAPI.user;

import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull Role role) {
}
