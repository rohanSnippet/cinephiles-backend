package com.projects.cinephiles.Service;

import com.projects.cinephiles.Enum.Role;
import com.projects.cinephiles.Repo.AdminRepo;
import com.projects.cinephiles.Repo.OwnerRepo;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.exceptions.UserAlreadyExistsException;
import com.projects.cinephiles.models.Admin;
import com.projects.cinephiles.models.Owner;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OwnerRepo ownerRepo;

    @Autowired
    private AdminRepo adminRepo;

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
        user.setRole(Role.THEATRE_OWNER);
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

    @Transactional
    public void assignRoleToUser(String username, Role newRole){
        Optional<User> optionalUser = userRepo.findByUsername(username);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            user.setRole(newRole);
            if(newRole == Role.THEATRE_OWNER){
                createOwnerForUser(user);
            } else if(newRole == Role.ADMIN){
                createAdminForUser(user);
            }
            userRepo.save(user);
        }
    }

    private void createOwnerForUser(User user) {
        Owner owner = new Owner();
        owner.setUser(user);
        owner.setRevenue(0.0f);
        ownerRepo.save(owner);
    }
    private void createAdminForUser(User user) {
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setRevenue(0.0f);
        adminRepo.save(admin);
    }


}
