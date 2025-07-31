package com.mlouis594.CommerceAPI.user;

import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User [" + username + "] NOT FOUND"
                ));
    }

    public List<UserDTO> getUsers(){
        return userRepository
                .findAll()
                .stream()
                .map(mapToUserDTO())
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(UUID id){
        return userRepository
                .findById(id)
                .map(mapToUserDTO())
                .orElseThrow(() ->new ResourceNotFound(
                        "User with ID [" + id + "] NOT FOUND"
                ));
    }

    public UUID saveUser(NewUserRequest userRequest){
        //TODO: Encode password before saving
        User u = new User();
        u.setUsername(userRequest.username());
        u.setFirstName(userRequest.firstName());
        u.setLastName(userRequest.lastName());
        u.setPassword(userRequest.password());
        userRepository.save(u);
        return u.getId();
    }

    public void updateUser(UUID id, NewUserRequest user){
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFound(
                "User with ID [" + id + "] NOT FOUND"
        ));

        if(user.username()!=null && user.username().equals(u.getUsername())){
            u.setUsername(user.username());
        }

        if(user.firstName()!=null && user.firstName().equals(u.getFirstName())){
            u.setFirstName(user.firstName());
        }

        if(user.lastName()!=null && user.lastName().equals(u.getLastName())){
            u.setLastName(user.lastName());
        }

        if(user.password()!=null && user.password().equals(u.getPassword())){
            u.setPassword(user.password());
        }
        userRepository.save(u);
    }

    public void deleteUser(UUID id){
        boolean exists = userRepository.existsById(id);

        if(!exists) throw new ResourceNotFound(
                "User with ID [" + id + "] NOT FOUND"
        );

        userRepository.deleteById(id);
    }

    private static Function<User, UserDTO> mapToUserDTO() {
        return user -> new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName());
    }
}
