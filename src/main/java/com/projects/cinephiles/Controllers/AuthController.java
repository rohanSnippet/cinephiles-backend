package com.projects.cinephiles.Controllers;


import com.projects.cinephiles.JwtConfig.JwtHelper;
import com.projects.cinephiles.JwtConfig.JwtRequest;
import com.projects.cinephiles.JwtConfig.JwtResponse;
import com.projects.cinephiles.Service.UserService;
import com.projects.cinephiles.exceptions.UserAlreadyExistsException;
import com.projects.cinephiles.models.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@AllArgsConstructor
@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private JwtHelper helper;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        //authenticate
        this.doAuthenticate(request.getUsername(), request.getPassword());


        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = this.helper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .username(userDetails.getUsername()).build();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }
    private void doAuthenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
        try {
            manager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credentials Invalid !!");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>("User already exists", HttpStatus.CONFLICT);
        }
    }

    @GetMapping("/admin")
    public String createUser() throws Exception {
        return "Hello Admin";
    }

}

