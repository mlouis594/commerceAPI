package com.mlouis594.CommerceAPI.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDTO> getUsers(){
        return userService.getUsers();
    }

    @GetMapping("{id}")
    public UserDTO getUserById(@PathVariable("id") UUID id){
        return userService.getUserById(id);
    }

    @PostMapping
    public UUID saveUser(@RequestBody NewUserRequest userRequest){
        return userService.saveUser(userRequest);
    }

    @PutMapping("{id}")
    public void updateUser(@PathVariable("id") UUID id, @RequestBody NewUserRequest userRequest){
        userService.updateUser(id, userRequest);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable UUID id){
        userService.deleteUser(id);
    }
}
