package com.mlouis594.CommerceAPI.order;

import java.util.Set;

public enum OrderStatus {
    PLACED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    /** Allowed lifecycle: PLACED -> SHIPPED -> DELIVERED, and PLACED -> CANCELLED. */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PLACED -> Set.of(SHIPPED, CANCELLED).contains(next);
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
