package com.mlouis594.CommerceAPI.Product;

import java.math.BigDecimal;

public record NewProductRequest(String name,
                                String description,
                                BigDecimal price,
                                Integer inventory,
                                String imageUrl) {

}
