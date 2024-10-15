package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.OwnerService;
import com.projects.cinephiles.Service.TheatreService;
import com.projects.cinephiles.models.Theatre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
