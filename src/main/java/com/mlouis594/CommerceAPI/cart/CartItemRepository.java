package com.mlouis594.CommerceAPI.cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @EntityGraph(attributePaths = "product")
    List<CartItem> findByUserIdOrderByProductName(UUID userId);

    /**
     * Products are left as lazy proxies so a subsequent locking query loads
     * them fresh rather than reusing possibly stale state.
     */
    List<CartItem> findByUserId(UUID userId);

    Optional<CartItem> findByUserIdAndProductId(UUID userId, UUID productId);

    @Modifying
    @Query("delete from CartItem c where c.user.id = :userId")
    void deleteAllByUserId(UUID userId);

    @Modifying
    @Query("delete from CartItem c where c.product.id = :productId")
    void deleteAllByProductId(UUID productId);
}
