package com.mlouis594.CommerceAPI.order;

import com.mlouis594.CommerceAPI.Product.Product;
import com.mlouis594.CommerceAPI.Product.ProductRepository;
import com.mlouis594.CommerceAPI.cart.AddCartItemRequest;
import com.mlouis594.CommerceAPI.cart.CartService;
import com.mlouis594.CommerceAPI.exception.InsufficientStock;
import com.mlouis594.CommerceAPI.support.IntegrationTest;
import com.mlouis594.CommerceAPI.user.User;
import com.mlouis594.CommerceAPI.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/** Many customers race for the last units; row locking must prevent overselling. */
class ConcurrentCheckoutTest extends IntegrationTest {

    @Autowired ProductRepository productRepository;
    @Autowired CartService cartService;
    @Autowired OrderService orderService;
    @Autowired UserService userService;

    @Test
    void concurrentCheckoutsNeverOversell() throws Exception {
        int stock = 3;
        int buyers = 10;
        UUID productId = productRepository.save(
                new Product(unique("Limited"), null, new BigDecimal("50.00"), stock, null)).getId();

        List<UUID> userIds = new ArrayList<>();
        for (int i = 0; i < buyers; i++) {
            String username = unique("racer");
            signup(username, "password123");
            UUID userId = ((User) userService.loadUserByUsername(username)).getId();
            cartService.addItem(userId, new AddCartItemRequest(productId, 1));
            userIds.add(userId);
        }

        ExecutorService pool = Executors.newFixedThreadPool(buyers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (UUID userId : userIds) {
            results.add(pool.submit(() -> {
                start.await();
                try {
                    orderService.checkout(userId);
                    return true;
                } catch (InsufficientStock e) {
                    return false;
                }
            }));
        }
        start.countDown();

        int succeeded = 0;
        for (Future<Boolean> r : results) {
            if (r.get(30, TimeUnit.SECONDS)) succeeded++;
        }
        pool.shutdown();

        assertThat(succeeded).isEqualTo(stock);
        assertThat(productRepository.findById(productId).orElseThrow().getInventory()).isZero();
    }
}
