package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.OwnerService;
import com.projects.cinephiles.Service.TheatreService;
import com.projects.cinephiles.models.Theatre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/theatre")
public class TheatreController {

    @Autowired
    private TheatreService theatreService;
    @Autowired
    private OwnerService ownerService;

    @PostMapping("/add-theatre")
    @PreAuthorize("hasRole('THEATRE_OWNER')")
    public Theatre addTheatre(@RequestBody Theatre theatre){
        return theatreService.addTheatre(theatre);
    }

    @GetMapping("/get-theatres/{username}")
    @PreAuthorize("hasRole('THEATRE_OWNER')")
    public List<Theatre> getTheatresByOwnerUsername(@PathVariable String username) {
        System.out.println("Controller:"+username);
        return ownerService.getTheatresByUsername(username);
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Theatre>> getAllTheatres() {
        try {
            List<Theatre> theatres = theatreService.getAllTheatres();
            return ResponseEntity.ok(theatres); // Returns a 200 OK response with the list of theaters
        } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList()); // Returns a 500 error with an empty list
        }
    }


    @GetMapping("/get-theatres/by-location")
    public List<Theatre> getTheatresByLocation(@RequestParam List<String> cities) {
        System.out.println("Controller:"+cities);
        return theatreService.getTheatresByLocation(cities);
    }
}
