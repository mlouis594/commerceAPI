package com.mlouis594.CommerceAPI;

import com.mlouis594.CommerceAPI.Product.Product;
import com.mlouis594.CommerceAPI.Product.ProductRepository;
import com.mlouis594.CommerceAPI.user.User;
import com.mlouis594.CommerceAPI.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@SpringBootApplication
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	String[] pName = {"Mackbook Pro", "Dell Air", "Lenovo Mixmaster"};
	String[] pDes = {"Mackbook Pro M3000", "Lakewood Series", "Lenovo Mix420"};
	BigDecimal[] pPrice = {new BigDecimal(5000), new BigDecimal(2000), new BigDecimal(999)};
	Integer[] pInv = {100, 323, 69};

	String[] uUname = {"markDaShark", "billyBadass", "franky2Times"};
	String[] uFname = {"Mark", "William", "Franklin"};
	String[] uLname = {"Vivet", "Shybin", "Tedward"};
	String[] uPass = {"kantDecodeMe123", "penTest", "nothing2Hide"};
	@Bean
	public CommandLineRunner commandLineRunner(ProductRepository productRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			for(int i=0; i<3; i++){
				Product product = new Product();
				product.setName(pName[i]);
				product.setDescription(pDes[i]);
				product.setPrice(pPrice[i]);
				product.setInventory(pInv[i]);
				productRepository.save(product);

				User user = new User();
				user.setUsername(uUname[i]);
				user.setFirstName(uFname[i]);
				user.setLastName(uLname[i]);
				user.setPassword(passwordEncoder.encode(uPass[i]));
				user.setRole("ROLE_USER");
				userRepository.save(user);
			}
		};
	}

}
