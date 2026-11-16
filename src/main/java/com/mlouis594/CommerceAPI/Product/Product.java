package com.mlouis594.CommerceAPI.Product;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer inventory;

    @Column(length = 200)
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant updatedAt;

    public Product(){

    }

    public Product(
            String name,
            String description,
            BigDecimal price,
            Integer inventory,
            String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.inventory = inventory;
        this.imageUrl = imageUrl;
    }

    @PrePersist //when adding a new product this function will be called and values updated
    public void prePersist () {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate //when object is updated this function will be called; managed by spring
    public void preUpdate(){
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getInventory() {
        return inventory;
    }

    public void setInventory(Integer inventory) {
        this.inventory = inventory;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Entity identity is the database id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return id != null && id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
