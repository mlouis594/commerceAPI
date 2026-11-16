package com.mlouis594.CommerceAPI.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(List<Item> items, int totalQuantity, BigDecimal total) {

    public record Item(UUID productId, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        static Item from(CartItem ci) {
            BigDecimal price = ci.getProduct().getPrice();
            return new Item(ci.getProduct().getId(), ci.getProduct().getName(), price, ci.getQuantity(),
                    price.multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
    }

    static CartResponse from(List<CartItem> cartItems) {
        List<Item> items = cartItems.stream().map(Item::from).toList();
        return new CartResponse(items,
                items.stream().mapToInt(Item::quantity).sum(),
                items.stream().map(Item::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
