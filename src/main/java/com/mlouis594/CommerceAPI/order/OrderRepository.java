package com.mlouis594.CommerceAPI.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"items", "user"})
    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {"items", "user"})
    List<Order> findAllByOrderByCreatedAtDesc();

    boolean existsByUserId(UUID userId);
}
