package com.projects.cinephiles.Service;

import com.projects.cinephiles.Enum.Role;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.exceptions.UserAlreadyExistsException;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
        if (user.getPassword() != null) user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER);
        return userRepo.save(user);
    }

    @Cacheable(value = "user", key="#username")
    public User getUserByUsername(String username) {
        System.out.println(username);
        return userRepo.getUserByUsername(username);
    }

    // --- Important method ---

    //@Cacheable(value = "user", key="#id")
    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }
    // -----------------------

    public boolean checkIfUserIsAdmin(String username) {
        User user = userRepo.getUserByUsername(username);
        return user != null && user.getRole() == Role.ADMIN;
    }

    public boolean checkIfUserIsOwner(String username) {
        User user = userRepo.getUserByUsername(username);
        return user != null && user.getRole() == Role.THEATRE_OWNER;
    }

    public User createUserFromOAuth2(OAuth2User oauth2User) {
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String email = oauth2User.getAttribute("email");
        String profile = oauth2User.getAttribute("picture");

        User newUser = new User();
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setUsername(email);
        newUser.setProfile(profile);
        newUser.setProvider("google");
        newUser.setRole(Role.USER);
        return userRepo.save(newUser);

    }

    @Transactional
    @CacheEvict(value = "user", key="#result.body.username")
    public ResponseEntity<User> updateUserById(Long id, User updatedUser) {
        Optional<User> optionalUser = userRepo.findById(id);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (updatedUser.getFirstName() != null) user.setFirstName(updatedUser.getFirstName());
            if (updatedUser.getLastName() != null) user.setLastName(updatedUser.getLastName());
            if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());
            if (updatedUser.getProfile() != null) user.setProfile(updatedUser.getProfile());
            if (updatedUser.getDob() != null) user.setDob(updatedUser.getDob());
            if (updatedUser.getGender() != null) user.setGender(updatedUser.getGender());
            if (updatedUser.getCurrLocation() != null) user.setCurrLocation(updatedUser.getCurrLocation());
            if (updatedUser.getAddressLine() != null) user.setAddressLine(updatedUser.getAddressLine());
            if (updatedUser.getCity() != null) user.setCity(updatedUser.getCity());
            if (updatedUser.getState() != null) user.setState(updatedUser.getState());
            if (updatedUser.getPincode() != null) user.setPincode(updatedUser.getPincode());
            if (updatedUser.getLandmark() != null) user.setLandmark(updatedUser.getLandmark());
            if (updatedUser.getPublicId() != null) user.setPublicId(updatedUser.getPublicId());
            if (updatedUser.getRole() != null) user.setRole(updatedUser.getRole());

            userRepo.save(user);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @CacheEvict(value = "user", key="#result.body.username")
    public ResponseEntity<User> updateLocationById(Long id, String currLocation) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setCurrLocation(currLocation);
            userRepo.save(user);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    public List<String> getUserCities(String email) {
        Optional<User> opUser = userRepo.findByUsername(email);
        List<String> cities = new ArrayList<>();
        if (opUser.isPresent()) {
            User user = opUser.get();
            if (user.getCurrLocation() != null) {
                cities.add(user.getCurrLocation());
            }
        }
        return cities;
    }
}