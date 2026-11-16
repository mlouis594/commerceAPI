package com.mlouis594.CommerceAPI.cart;

import com.mlouis594.CommerceAPI.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal User me) {
        return cartService.getCart(me.getId());
    }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal User me, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(me.getId(), request);
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateItem(@AuthenticationPrincipal User me,
                                   @PathVariable UUID productId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(me.getId(), productId, request.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal User me, @PathVariable UUID productId) {
        return cartService.removeItem(me.getId(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal User me) {
        cartService.clear(me.getId());
    }
}
