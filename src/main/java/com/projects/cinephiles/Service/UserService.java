package com.projects.cinephiles.Service;

import com.projects.cinephiles.Enum.Role;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.exceptions.UserAlreadyExistsException;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepo;

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Transactional
    public User createUser(User user) throws UserAlreadyExistsException {
        Optional<User> existingUser = userRepo.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepo.save(user);
    }

    public User getUserByUsername(String username) {
        return userRepo.getUserByUsername(username);
    }


    public boolean checkIfUserIsAdmin(String username) {
        User user = userRepo.getUserByUsername(username);
        return user.getRole()==Role.ADMIN;
    }

    public boolean checkIfUserIsOwner(String username) {
        User user = userRepo.getUserByUsername(username);
        return user.getRole()==Role.THEATRE_OWNER;
    }

    public User createUserFromOAuth2(OAuth2User oauth2User) {
        // Extract user information from OAuth2User
        String username = oauth2User.getAttribute("name");
        String email = oauth2User.getAttribute("email");

        // Create and save new user in your database
        User newUser = new User();
        if (username == null) throw new AssertionError();
        newUser.setFirstName(username.split(" ")[0]);
        newUser.setLastName(username.split(" ")[1]);
        newUser.setUsername(email);
        // Set other fields as necessary (e.g., role, password, etc.)
        return userRepo.save(newUser);
    }

    @Transactional
    public ResponseEntity<User> updateUserById(Long id, User updatedUser) {
        Optional<User> optionalUser = userRepo.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Update fields if they are provided in updatedUser
            if (updatedUser.getFirstName() != null) {
                user.setFirstName(updatedUser.getFirstName());
            }
            if (updatedUser.getLastName() != null) {
                user.setLastName(updatedUser.getLastName());
            }
            if (updatedUser.getPhone() != null) {
                user.setPhone(updatedUser.getPhone());
            }
            if (updatedUser.getProfile() != null) {
                user.setProfile(updatedUser.getProfile());
            }
            if (updatedUser.getDob() != null) {
                user.setDob(updatedUser.getDob());
            }
            if (updatedUser.getGender() != null) {
                user.setGender(updatedUser.getGender());
            }
            if (updatedUser.getCurrLocation() != null) {
                user.setCurrLocation(updatedUser.getCurrLocation());
            }
            if (updatedUser.getAddressLine() != null) {
                user.setAddressLine(updatedUser.getAddressLine());
            }
            if (updatedUser.getCity() != null) {
                user.setCity(updatedUser.getCity());
            }
            if (updatedUser.getState() != null) {
                user.setState(updatedUser.getState());
            }
            if (updatedUser.getPincode() != null) {
                user.setPincode(updatedUser.getPincode());
            }
            if (updatedUser.getLandmark() != null) {
                user.setLandmark(updatedUser.getLandmark());
            }
            if (updatedUser.getPublicId() != null) {
                user.setPublicId(updatedUser.getPublicId());
            }

            // Update role if provided
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }

            // If the password is being updated, encode it
//            if (updatedUser.getPassword() != null && !updatedUser.getPassword().equals(user.getPassword())) {
//                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
//            }

            // Save the updated user
            userRepo.save(user);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    public ResponseEntity<User> updateLocationById(Long id, String currLocation) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setCurrLocation(currLocation); // Update the currLocation field
            System.out.println(currLocation);
            userRepo.save(user); // Save the updated user back to the database

            return ResponseEntity.ok(user); // Return the updated user object
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if user not found
        }
    }

    public List<String> getUserCities(String email) {
        Optional<User> opUser = userRepo.findByUsername(email);

        List<String> cities = new ArrayList<>();

        if (opUser.isPresent()) {
            User user = opUser.get();
            if (user.getCurrLocation() != null) {
                // Add the city to the list (even if there's only one city)
                cities.add(user.getCurrLocation());
            }
            System.out.println(user.getCurrLocation());
        }

        System.out.println(cities);
        return cities;
    }
}
