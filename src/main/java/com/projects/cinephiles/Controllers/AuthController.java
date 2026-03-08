package com.projects.cinephiles.Controllers;


import com.projects.cinephiles.JwtConfig.JwtHelper;
import com.projects.cinephiles.JwtConfig.JwtRequest;
import com.projects.cinephiles.JwtConfig.JwtResponse;
import com.projects.cinephiles.Service.UserService;
import com.projects.cinephiles.exceptions.UserAlreadyExistsException;
import com.projects.cinephiles.models.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(Authentication authentication) {
        // 1. Check if authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "User is not authenticated"));
        }

        Map<String, Object> response = new HashMap<>();
        Object principal = authentication.getPrincipal();

        // 2. Handle different Principal types
        if (principal instanceof OAuth2User) {
            // Case A: Session-based OAuth2 login (Initial login)
            OAuth2User oauthUser = (OAuth2User) principal;
            response.putAll(oauthUser.getAttributes());
            // Ensure email is set commonly
            response.put("email", oauthUser.getAttribute("email"));

        } else if (principal instanceof UserDetails) {
            // Case B: JWT Token login (Subsequent requests)
            UserDetails userDetails = (UserDetails) principal;
            response.put("email", userDetails.getUsername());
            response.put("username", userDetails.getUsername());
            // Add other details if your UserDetails implementation has them

        } else if (principal instanceof String) {
            // Case C: Simple string principal
            response.put("email", principal.toString());
            response.put("username", principal.toString());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String,String>> getUserInfo(HttpServletRequest request) {
        // Return user attributes if authenticated
        String token = (String) request.getSession().getAttribute("access-token");
        System.out.println("callback token :"+token);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token not found"));
        }

        return ResponseEntity.ok(Map.of("token", token));
    }

//    @PostMapping("/logout")
//   // @CacheEvict(value = "user", key = "")
//    public ResponseEntity<?> oauthLogout(HttpServletRequest request){
//        System.out.println("logout hit"+ request);
//        request.getSession().invalidate();
//        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
//    }

    @PostMapping("/logout")
    @CacheEvict(value = "user", key = "#principal.name", condition = "#principal != null")
    public ResponseEntity<?> oauthLogout(HttpServletRequest request, Principal principal) {

        // 1. Extract the username from the Principal
        if (principal != null) {
            System.out.println("Logout hit for user: " + principal.getName());
        } else {
            System.out.println("Logout hit for an unauthenticated request");
        }

        // 2. Invalidate the session (Useful for Google OAuth2 stateful sessions)
        // Using false prevents creating a NEW session just to invalidate it
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 3. Clear the Security Context to instantly revoke access in the current thread
        SecurityContextHolder.clearContext();

        // 4. Return success message
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

}

