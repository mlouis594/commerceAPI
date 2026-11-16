package com.mlouis594.CommerceAPI.order;

import org.junit.jupiter.api.Test;

import static com.mlouis594.CommerceAPI.order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void placedCanBeShippedOrCancelled() {
        assertThat(PLACED.canTransitionTo(SHIPPED)).isTrue();
        assertThat(PLACED.canTransitionTo(CANCELLED)).isTrue();
        assertThat(PLACED.canTransitionTo(DELIVERED)).isFalse();
        assertThat(PLACED.canTransitionTo(PLACED)).isFalse();
    }

    @Test
    void shippedCanOnlyBeDelivered() {
        assertThat(SHIPPED.canTransitionTo(DELIVERED)).isTrue();
        assertThat(SHIPPED.canTransitionTo(CANCELLED)).isFalse();
        assertThat(SHIPPED.canTransitionTo(PLACED)).isFalse();
    }

    @Test
    void terminalStatesAreFinal() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(DELIVERED.canTransitionTo(next)).isFalse();
            assertThat(CANCELLED.canTransitionTo(next)).isFalse();
        }
    }
}
