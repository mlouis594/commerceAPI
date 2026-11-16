package com.mlouis594.CommerceAPI.Product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** All fields optional; only non-null fields are applied. */
public record UpdateProductRequest(
        @Size(min = 1, max = 50) String name,
        @Size(max = 255) String description,
        @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @PositiveOrZero Integer inventory,
        @Size(max = 200) String imageUrl) {
}
