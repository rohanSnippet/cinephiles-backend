package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Enum.Role;
import com.projects.cinephiles.Service.TheatreRequestService;
import com.projects.cinephiles.Service.UserService;
import com.projects.cinephiles.models.TheatreRequest;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TheatreRequestService theatreRequestService;

    @GetMapping
    public User getUserByUsername(@RequestParam String username){
        return userService.getUserByUsername(username);
    }

    @GetMapping("/is-admin")
    public boolean checkIfUserIsAdmin(@RequestParam String username) {
      return userService.checkIfUserIsAdmin(username);
    }

    @GetMapping("/is-owner")
    public boolean checkIfUserIsOwner(@RequestParam String username){
        return userService.checkIfUserIsOwner(username);
    }

    @PostMapping("/make-request")
    public TheatreRequest makeTheatreRequest(@RequestBody TheatreRequest theatreRequest){
      return theatreRequestService.makeRequest(theatreRequest);
    }

}
