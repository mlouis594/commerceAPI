package com.mlouis594.CommerceAPI.user;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Self-service endpoints live under /me; everything addressed by id is
 * admin-only (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UUID signup(@Valid @RequestBody NewUserRequest userRequest) {
        return userService.saveUser(userRequest);
    }

    // --- current user ---

    @GetMapping("/me")
    public UserDTO getMe(@AuthenticationPrincipal User me) {
        return UserDTO.from(me);
    }

    @PutMapping("/me")
    public void updateMe(@AuthenticationPrincipal User me, @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(me.getId(), request);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMe(@AuthenticationPrincipal User me) {
        userService.deleteUser(me.getId());
    }

    // --- admin ---

    @GetMapping
    public List<UserDTO> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request);
    }

    @PutMapping("/{id}/role")
    public void changeRole(@PathVariable UUID id, @Valid @RequestBody ChangeRoleRequest request) {
        userService.changeRole(id, request.role());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
    }
}
