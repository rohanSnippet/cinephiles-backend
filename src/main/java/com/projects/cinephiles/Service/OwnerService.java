package com.projects.cinephiles.Service;

import com.projects.cinephiles.Enum.Role;
import com.projects.cinephiles.Repo.OwnerRepo;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.models.Owner;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerService {
    @Autowired
    private OwnerRepo ownerRepo;

    @Autowired
    private UserRepo userRepo;

    public List<Owner> getAllOwners() {
        return ownerRepo.findAll();
    }

    public Owner makeUserOwner(String username) {
        User user = userRepo.getUserByUsername(username);

        if (user == null) {
            throw new IllegalArgumentException("User with username " + username + " not found.");
        }

        user.setRole(Role.THEATRE_OWNER);
        userRepo.save(user);
        System.out.println("Changed Role in User table to: " + Role.THEATRE_OWNER);

        Owner owner = new Owner();
        owner.setUser(user);
        owner.setRevenue(0.0f);
        return ownerRepo.save(owner);
    }
}
