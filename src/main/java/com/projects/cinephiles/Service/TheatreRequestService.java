package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.TheatreRequestRepo;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.models.TheatreRequest;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheatreRequestService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    TheatreRequestRepo theatreRequestRepo;

    public TheatreRequest makeRequest(TheatreRequest theatreRequest) {
        User requestUser = userRepo.getUserByUsername(theatreRequest.getUsername());
        theatreRequest.setUser(requestUser);
        return theatreRequestRepo.save(theatreRequest);
    }

    public List<TheatreRequest> getAllRequests() {
        return theatreRequestRepo.findAll();
    }

}
