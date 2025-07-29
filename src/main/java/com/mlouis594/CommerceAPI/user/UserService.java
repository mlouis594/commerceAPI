package com.mlouis594.CommerceAPI.user;

import com.mlouis594.CommerceAPI.exception.ResourceNotFound;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
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
        UUID id = UUID.randomUUID();
        //TODO: Encode password before saving
        userRepository.save(
                new User(
                        id,
                        userRequest.userName(),
                        userRequest.firstName(),
                        userRequest.lastName(),
                        userRequest.password()
                )
        );
        return id;
    }

    public void updateUser(UUID id, NewUserRequest user){
        User u = userRepository.findById(id).orElseThrow(() -> new ResourceNotFound(
                "User with ID [" + id + "] NOT FOUND"
        ));

        if(user.userName()!=null && user.userName().equals(u.getUserName())){
            u.setUserName(user.userName());
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
                user.getUserName(),
                user.getFirstName(),
                user.getLastName());
    }
}
