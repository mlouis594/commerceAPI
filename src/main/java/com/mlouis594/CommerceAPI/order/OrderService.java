package com.mlouis594.CommerceAPI.order;

import com.mlouis594.CommerceAPI.Product.Product;
import com.mlouis594.CommerceAPI.Product.ProductRepository;
import com.mlouis594.CommerceAPI.cart.CartItem;
import com.mlouis594.CommerceAPI.cart.CartItemRepository;
import com.mlouis594.CommerceAPI.exception.InsufficientStock;
import com.mlouis594.CommerceAPI.exception.InvalidRequest;
import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
import com.mlouis594.CommerceAPI.user.Role;
import com.mlouis594.CommerceAPI.user.User;
import com.mlouis594.CommerceAPI.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Turns the user's cart into an order: locks the products, verifies and
     * decrements stock, snapshots prices, and empties the cart, all atomically.
     */
    @Transactional
    public OrderResponse checkout(UUID userId) {
        List<CartItem> cart = cartItemRepository.findByUserId(userId);
        if (cart.isEmpty()) {
            throw new InvalidRequest("Cannot check out an empty cart");
        }

        // Lock before reading inventory: cart products are still uninitialised proxies,
        // so this loads committed, up-to-date stock under the row lock.
        Map<UUID, Product> locked = productRepository
                .findAllByIdForUpdate(cart.stream().map(ci -> ci.getProduct().getId()).toList())
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order(userRepository.getReferenceById(userId));
        for (CartItem ci : cart) {
            Product p = locked.get(ci.getProduct().getId());
            if (p.getInventory() < ci.getQuantity()) {
                throw new InsufficientStock("Only " + p.getInventory() + " of [" + p.getName() + "] in stock");
            }
            p.setInventory(p.getInventory() - ci.getQuantity());
            order.addItem(new OrderItem(p.getId(), p.getName(), p.getPrice(), ci.getQuantity()));
        }

        orderRepository.save(order);
        cartItemRepository.deleteAllByUserId(userId);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(User requester, UUID orderId) {
        return OrderResponse.from(findVisibleOrder(requester, orderId));
    }

    /** Customers may cancel their own order while it is still PLACED; stock is returned. */
    @Transactional
    public OrderResponse cancel(User requester, UUID orderId) {
        Order order = findVisibleOrder(requester, orderId);
        transition(order, OrderStatus.CANCELLED);
        return OrderResponse.from(order);
    }

    /** Admin-only status change, following the OrderStatus lifecycle. */
    @Transactional
    public OrderResponse updateStatus(UUID orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> notFound(orderId));
        transition(order, status);
        return OrderResponse.from(order);
    }

    private void transition(Order order, OrderStatus next) {
        if (!order.getStatus().canTransitionTo(next)) {
            throw new InvalidRequest("Cannot change order from " + order.getStatus() + " to " + next);
        }
        if (next == OrderStatus.CANCELLED) {
            restock(order);
        }
        order.setStatus(next);
    }

    private void restock(Order order) {
        List<UUID> ids = order.getItems().stream().map(OrderItem::getProductId).toList();
        Map<UUID, Product> products = productRepository.findAllByIdForUpdate(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (OrderItem item : order.getItems()) {
            Product p = products.get(item.getProductId());
            if (p != null) { // product may have been deleted since the order was placed
                p.setInventory(p.getInventory() + item.getQuantity());
            }
        }
    }

    /** Owners and admins can see an order; anyone else gets 404 so ids are not leaked. */
    private Order findVisibleOrder(User requester, UUID orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> notFound(orderId));
        boolean isOwner = order.getUser().getId().equals(requester.getId());
        if (!isOwner && requester.getRole() != Role.ADMIN) {
            throw notFound(orderId);
        }
        return order;
    }

    private static ResourceNotFound notFound(UUID orderId) {
        return new ResourceNotFound("Order with ID [" + orderId + "] NOT FOUND");
    }
}
