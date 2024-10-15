package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.TheatreRequestService;
import com.projects.cinephiles.Service.UserService;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PutMapping("/update-user/{id}")
    public ResponseEntity<User> UpdateUserById(@PathVariable Long id, @RequestBody User user){
        return userService.updateUserById( id, user);
    }

    @PutMapping("/update-location/{id}")
    public ResponseEntity<User> updateLocation(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        String currLocation = requestBody.get("currLocation");
        System.out.println("Received currLocation: " + currLocation); // Debug log
        return userService.updateLocationById(id, currLocation);
    }

}
