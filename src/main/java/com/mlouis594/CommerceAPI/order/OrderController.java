package com.mlouis594.CommerceAPI.order;

import com.mlouis594.CommerceAPI.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse checkout(@AuthenticationPrincipal User me) {
        return orderService.checkout(me.getId());
    }

    @GetMapping
    public List<OrderResponse> myOrders(@AuthenticationPrincipal User me) {
        return orderService.getOrdersForUser(me.getId());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@AuthenticationPrincipal User me, @PathVariable UUID id) {
        return orderService.getOrder(me, id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal User me, @PathVariable UUID id) {
        return orderService.cancel(me, id);
    }

    // --- admin (enforced in SecurityConfig) ---

    @GetMapping("/all")
    public List<OrderResponse> allOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }
}
