package com.mlouis594.CommerceAPI.cart;

import com.mlouis594.CommerceAPI.Product.Product;
import com.mlouis594.CommerceAPI.Product.ProductRepository;
import com.mlouis594.CommerceAPI.exception.InsufficientStock;
import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
import com.mlouis594.CommerceAPI.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        return CartResponse.from(cartItemRepository.findByUserIdOrderByProductName(userId));
    }

    /** Adds the product to the cart, or increases the quantity if it is already there. */
    @Transactional
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFound("Product with ID [" + request.productId() + "] NOT FOUND"));

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, product.getId())
                .orElseGet(() -> new CartItem(userRepository.getReferenceById(userId), product, 0));
        int newQuantity = item.getQuantity() + request.quantity();
        ensureInStock(product, newQuantity);
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return getCart(userId);
    }

    @Transactional
    public CartResponse updateItem(UUID userId, UUID productId, int quantity) {
        CartItem item = findItem(userId, productId);
        ensureInStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID productId) {
        cartItemRepository.delete(findItem(userId, productId));
        return getCart(userId);
    }

    @Transactional
    public void clear(UUID userId) {
        cartItemRepository.deleteAllByUserId(userId);
    }

    private CartItem findItem(UUID userId, UUID productId) {
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFound("Product with ID [" + productId + "] is not in your cart"));
    }

    // Soft check for early feedback; stock is re-checked under lock at checkout
    private static void ensureInStock(Product product, int quantity) {
        if (quantity > product.getInventory()) {
            throw new InsufficientStock("Only " + product.getInventory() + " of [" + product.getName() + "] in stock");
        }
    }
}
