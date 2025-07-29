package com.mlouis594.CommerceAPI.Product;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts(){
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable("id") UUID id){
        return productService.getProductById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable("id") UUID id){
        productService.deleteProductById(id);
    }

    @PostMapping
    public UUID saveProduct(@RequestBody NewProductRequest product){
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public void updateProduct(@PathVariable("id") UUID id, @RequestBody UpdateProductRequest product){
        productService.updateProduct(id, product);
    }
}
