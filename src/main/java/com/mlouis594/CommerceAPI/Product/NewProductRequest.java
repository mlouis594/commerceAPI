package com.mlouis594.CommerceAPI.Product;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record NewProductRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 255) String description,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull @PositiveOrZero Integer inventory,
        @Size(max = 200) String imageUrl) {
}
