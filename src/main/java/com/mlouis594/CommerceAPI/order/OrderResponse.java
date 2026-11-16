package com.mlouis594.CommerceAPI.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id,
                            UUID userId,
                            OrderStatus status,
                            List<Item> items,
                            BigDecimal total,
                            Instant createdAt,
                            Instant updatedAt) {

    public record Item(UUID productId, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        static Item from(OrderItem i) {
            return new Item(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(), i.getLineTotal());
        }
    }

    static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getUser().getId(), o.getStatus(),
                o.getItems().stream().map(Item::from).toList(),
                o.getTotal(), o.getCreatedAt(), o.getUpdatedAt());
    }
}
