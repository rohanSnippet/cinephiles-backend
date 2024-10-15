package com.projects.cinephiles.Service;

import com.projects.cinephiles.Enum.Role;
import com.projects.cinephiles.Repo.OwnerRepo;
import com.projects.cinephiles.Repo.TheatreRepo;
import com.projects.cinephiles.Repo.TheatreRequestRepo;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.models.Owner;
import com.projects.cinephiles.models.Theatre;
import com.projects.cinephiles.models.TheatreRequest;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {
    @Autowired
    private OwnerRepo ownerRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TheatreRequestRepo theatreRequestRepo;

    @Autowired
    private TheatreRepo theatreRepo;

    public List<Owner> getAllOwners() {
        return ownerRepo.findAll();
    }

    @Transactional
    public Owner makeUserOwner(String username, Long id) {
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
        ownerRepo.save(owner);

        Optional<TheatreRequest> optionalReq = theatreRequestRepo.findById(id);
        TheatreRequest theatreRequest = optionalReq.get();
        theatreRequest.setStatus(TheatreRequest.RequestStatus.APPROVED);

        Theatre theatre = new Theatre();
        theatre.setName(theatreRequest.getTname());
        theatre.setTscreens(theatreRequest.getTscreens());
        theatre.setAddress(theatreRequest.getAddress());
        theatre.setContact(theatreRequest.getContact());
        theatre.setCity(theatreRequest.getTlocation());
        theatre.setState(theatreRequest.getState());
        theatre.setOwner(owner);
        theatreRepo.save(theatre);

        return owner;
    }

    public List<Theatre> getTheatresByUsername(String username) {
        // Retrieve the User based on their username
        Optional<User> userOptional = userRepo.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with username " + username + " not found.");
        }

        User user = userOptional.get();
        System.out.println("user is " + user.getUsername());
        // Retrieve the Owner from the User
       Owner owner = user.getOwner();
        if (owner == null) {
            throw new IllegalArgumentException("Owner associated with user " + username + " not found.");
        }
        System.out.println("Owner is: "+owner.getTheatres());
        // Return the list of theatres owned by the owner
        return owner.getTheatres();
    }

}
