package com.projects.cinephiles.Config;

import com.projects.cinephiles.JwtConfig.JwtHelper;
import com.projects.cinephiles.Service.UserService;
import com.projects.cinephiles.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private JwtHelper jwtHelper;
    private UserService userService;

    public CustomOAuth2SuccessHandler(UserService userService,JwtHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
        this.userService = userService;
    }

    // This is the method that will be called when authentication is successful
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Redirect the user to the homepage after successful authentication
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String username = principal.getAttribute("email");  // or another identifier
        System.out.println(principal);

        User user = userService.getUserByUsername(username);
        System.out.println(user);
        if (user == null) {
            user = userService.createUserFromOAuth2(principal);

        }
        // Generate the JWT token here
        String token = jwtHelper.generateToken(user);
        System.out.println("username is : "+username+"  Token is :"+token);

        response.setHeader("Authorization", "Bearer " + token);
        response.setHeader("X-Username", username);
        response.sendRedirect("http://localhost:5173/");  // Change this to your desired redirect URL
    }
}

