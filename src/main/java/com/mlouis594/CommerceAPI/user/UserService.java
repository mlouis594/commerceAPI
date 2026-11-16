package com.mlouis594.CommerceAPI.user;

import com.mlouis594.CommerceAPI.cart.CartItemRepository;
import com.mlouis594.CommerceAPI.exception.ResourceConflict;
import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
import com.mlouis594.CommerceAPI.order.OrderRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CartItemRepository cartItemRepository,
                       OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User [" + username + "] NOT FOUND"));
    }

    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream().map(UserDTO::from).toList();
    }

    public UserDTO getUserById(UUID id) {
        return UserDTO.from(findUser(id));
    }

    @Transactional
    public UUID saveUser(NewUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceConflict("Username [" + request.username() + "] is already taken");
        }
        User u = new User();
        u.setUsername(request.username());
        u.setFirstName(request.firstName());
        u.setLastName(request.lastName());
        u.setPassword(passwordEncoder.encode(request.password()));
        u.setRole(Role.USER);
        return userRepository.save(u).getId();
    }

    @Transactional
    public void updateUser(UUID id, UpdateUserRequest request) {
        User u = findUser(id);

        if (request.firstName() != null) {
            u.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            u.setLastName(request.lastName());
        }
        // BCrypt salts every hash, so compare with matches() rather than re-encoding
        if (request.password() != null && !passwordEncoder.matches(request.password(), u.getPassword())) {
            u.setPassword(passwordEncoder.encode(request.password()));
        }
    }

    @Transactional
    public void changeRole(UUID id, Role role) {
        findUser(id).setRole(role);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findUser(id);
        // Keep order history intact rather than silently deleting financial records
        if (orderRepository.existsByUserId(id)) {
            throw new ResourceConflict("User with ID [" + id + "] has order history and cannot be deleted");
        }
        cartItemRepository.deleteAllByUserId(id);
        userRepository.delete(user);
    }

    /** Resolves the authenticated principal to its persisted entity. */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFound("User [" + username + "] NOT FOUND"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("User with ID [" + id + "] NOT FOUND"));
    }
}
