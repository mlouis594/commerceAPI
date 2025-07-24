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
    private Instant deletedAt;

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

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
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

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return inventory == product.inventory && Objects.equals(id, product.id) && Objects.equals(name, product.name) && Objects.equals(description, product.description) && Objects.equals(price, product.price) && Objects.equals(imageUrl, product.imageUrl) && Objects.equals(createdAt, product.createdAt) && Objects.equals(updatedAt, product.updatedAt) && Objects.equals(deletedAt, product.deletedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, price, inventory, imageUrl, createdAt, updatedAt, deletedAt);
    }
}
