package com.mlouis594.CommerceAPI.config;

import com.mlouis594.CommerceAPI.Product.Product;
import com.mlouis594.CommerceAPI.Product.ProductRepository;
import com.mlouis594.CommerceAPI.user.Role;
import com.mlouis594.CommerceAPI.user.User;
import com.mlouis594.CommerceAPI.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Populates an empty database with demo products, demo users and an admin account. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public DataSeeder(ProductRepository productRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${app.seed.admin-username}") String adminUsername,
                      @Value("${app.seed.admin-password}") String adminPassword) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        productRepository.save(new Product("MacBook Pro", "M3 Max, 36GB RAM", new BigDecimal("3499.00"), 100, null));
        productRepository.save(new Product("Dell XPS 13", "Lakewood series ultrabook", new BigDecimal("1299.00"), 323, null));
        productRepository.save(new Product("Lenovo ThinkPad X1", "Carbon Gen 12", new BigDecimal("1899.00"), 69, null));

        createUser(adminUsername, "Store", "Admin", adminPassword, Role.ADMIN);
        createUser("markDaShark", "Mark", "Vivet", "kantDecodeMe123", Role.USER);
        createUser("billyBadass", "William", "Shybin", "penTest12345", Role.USER);
        createUser("franky2Times", "Franklin", "Tedward", "nothing2Hide", Role.USER);

        log.info("Seeded demo data (admin user: {})", adminUsername);
    }

    private void createUser(String username, String first, String last, String password, Role role) {
        User u = new User();
        u.setUsername(username);
        u.setFirstName(first);
        u.setLastName(last);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role);
        userRepository.save(u);
    }
}
