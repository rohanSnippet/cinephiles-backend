package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.TheatreRequestService;
import com.projects.cinephiles.models.TheatreRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TheatreRequestController {

    @Autowired
    TheatreRequestService theatreRequestService;

    @PostMapping("/make-request")
    public TheatreRequest makeTheatreRequest(@RequestBody TheatreRequest theatreRequest){
        return theatreRequestService.makeRequest(theatreRequest);
    }

    @GetMapping("/get-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TheatreRequest>> getAllReq() {
        List<TheatreRequest> theatreRequests = theatreRequestService.getAllRequests();
        return ResponseEntity.ok(theatreRequests); // Wrap the list in a ResponseEntity
    }


}
