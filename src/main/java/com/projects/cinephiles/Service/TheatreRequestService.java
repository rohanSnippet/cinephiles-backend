package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.TheatreRequestRepo;
import com.projects.cinephiles.models.TheatreRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheatreRequestService {

    @Autowired
    private TheatreRequestRepo theatreRequestRepo;
    public TheatreRequest makeRequest(TheatreRequest theatreRequest) {
        return theatreRequestRepo.save(theatreRequest);
    }

    public ResponseEntity<List<TheatreRequest>> getAllRequests() {
        return (ResponseEntity<List<TheatreRequest>>) theatreRequestRepo.findAll();
    }
}
