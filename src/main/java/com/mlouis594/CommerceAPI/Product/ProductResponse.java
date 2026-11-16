package com.mlouis594.CommerceAPI.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(UUID id,
                              String name,
                              String description,
                              BigDecimal price,
                              Integer inventory,
                              String imageUrl,
                              Instant createdAt,
                              Instant updatedAt) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getInventory(), p.getImageUrl(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
