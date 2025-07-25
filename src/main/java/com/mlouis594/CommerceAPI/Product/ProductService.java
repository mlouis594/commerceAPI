package com.mlouis594.CommerceAPI.Product;

import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts(){
        return productRepository
                .findAll()
                .stream()
                .map(mapToProductResponse())
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(UUID id){
        return productRepository
                .findById(id).map(mapToProductResponse())
                .orElseThrow( () -> new ResourceNotFound(
                        "Product with ID [" + id + "] NOT FOUND"
                ));
    }

    public void deleteProductById(UUID id){
        boolean exists = productRepository.existsById(id);
        if(!exists) throw new ResourceNotFound(
                "Product with ID [" + id + "] NOT FOUND"
        );

        productRepository.deleteById(id);
    }

    public UUID saveProduct(NewProductRequest product){
        UUID id = UUID.randomUUID();
        productRepository.save(new Product(
                id,
                product.name(),
                product.description(),
                product.price(),
                product.inventory(),
                product.imageUrl()
        ));
        return id;
    }

    public void updateProduct(UUID id, UpdateProductRequest request){

        Product p = productRepository.findById(id).orElseThrow(() -> new ResourceNotFound(
                "Product with ID [" + id + "] NOT FOUND"
        ));

        if(request.name() != null && !request.name().equals(p.getName())) {
            p.setName(request.name());
        }
        if(request.description() != null && !request.description().equals(p.getDescription())) {
            p.setDescription(request.description());
        }
        if(request.price() != null && !request.price().equals(p.getPrice())) {
            p.setPrice(request.price());
        }
        if(request.name() != null && !request.inventory().equals(p.getInventory())) {
            p.setInventory(request.inventory());
        }
        if(request.imageUrl() != null && !request.imageUrl().equals(p.getImageUrl())) {
            p.setImageUrl(request.imageUrl());
        }

        productRepository.save(p);
    }

    private static Function<Product, ProductResponse> mapToProductResponse() {
        return p -> new ProductResponse(p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getInventory(),
                p.getImageUrl(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                p.getDeletedAt());
    }


}
