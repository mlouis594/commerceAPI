package com.mlouis594.CommerceAPI.user;

import com.mlouis594.CommerceAPI.exception.DuplicateResource;
import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            throw new DuplicateResource("Username [" + request.username() + "] is already taken");
        }
        User u = new User();
        u.setUsername(request.username());
        u.setFirstName(request.firstName());
        u.setLastName(request.lastName());
        u.setPassword(passwordEncoder.encode(request.password()));
        u.setRole("ROLE_USER");
        return userRepository.save(u).getId();
    }

    @Transactional
    public void updateUser(UUID id, NewUserRequest request) {
        User u = findUser(id);

        if (request.username() != null && !request.username().equals(u.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new DuplicateResource("Username [" + request.username() + "] is already taken");
            }
            u.setUsername(request.username());
        }
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
    public void deleteUser(UUID id) {
        userRepository.delete(findUser(id));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("User with ID [" + id + "] NOT FOUND"));
    }
}
