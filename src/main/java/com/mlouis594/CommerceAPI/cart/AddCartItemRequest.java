package com.mlouis594.CommerceAPI.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddCartItemRequest(@NotNull UUID productId,
                                 @NotNull @Positive @Max(1000) Integer quantity) {
}
