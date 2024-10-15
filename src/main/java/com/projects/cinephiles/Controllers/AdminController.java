package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.*;
import com.projects.cinephiles.models.Owner;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    UserService userService;

    @Autowired
    OwnerService ownerService;

    @Autowired
    AdminService adminService;

    @Autowired
    TheatreRequestService theatreRequestService;

    @Autowired
    TheatreService theatreService;

    @GetMapping("/all-users")
        public List<User> getAllUsers(){
        return userService.getAllUsers();
        }

        @GetMapping("/all-owners")
        public List<Owner> getAllOwners(){
        return ownerService.getAllOwners();
        }


    @PutMapping("/make-owner")
    public ResponseEntity<Owner> makeUserOwner(@RequestParam String username, @RequestParam Long id) {
        try {
            Owner owner = ownerService.makeUserOwner(username,id);
            return ResponseEntity.ok(owner);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}
