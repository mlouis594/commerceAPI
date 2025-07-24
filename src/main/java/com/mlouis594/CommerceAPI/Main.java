package com.mlouis594.CommerceAPI;

import com.mlouis594.CommerceAPI.Product.Product;
import com.mlouis594.CommerceAPI.Product.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ProductRepository productRepository) {
		return args -> {
			Product product = new Product();
			product.setName("Mackbook Pro");
			product.setDescription("Mackbook Pro M3000");
			product.setPrice(new BigDecimal(5000));
			product.setInventory(100);
			productRepository.save(product);
		};
	}

}
