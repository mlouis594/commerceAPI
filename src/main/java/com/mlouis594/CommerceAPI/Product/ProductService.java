package com.mlouis594.CommerceAPI.Product;

import com.mlouis594.CommerceAPI.cart.CartItemRepository;
import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public ProductService(ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getProductById(UUID id) {
        return ProductResponse.from(findProduct(id));
    }

    @Transactional
    public void deleteProductById(UUID id) {
        Product product = findProduct(id);
        // Orders keep a snapshot of the product, so only carts reference it
        cartItemRepository.deleteAllByProductId(id);
        productRepository.delete(product);
    }

    @Transactional
    public UUID saveProduct(NewProductRequest request) {
        // Let JPA generate the id; assigning one by hand turns save() into a failing merge
        Product saved = productRepository.save(new Product(
                request.name(),
                request.description(),
                request.price(),
                request.inventory(),
                request.imageUrl()));
        return saved.getId();
    }

    @Transactional
    public void updateProduct(UUID id, UpdateProductRequest request) {
        Product p = findProduct(id);

        if (request.name() != null) p.setName(request.name());
        if (request.description() != null) p.setDescription(request.description());
        if (request.price() != null) p.setPrice(request.price());
        if (request.inventory() != null) p.setInventory(request.inventory());
        if (request.imageUrl() != null) p.setImageUrl(request.imageUrl());
        // Changes are flushed by dirty checking when the transaction commits
    }

    private Product findProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Product with ID [" + id + "] NOT FOUND"));
    }
}
