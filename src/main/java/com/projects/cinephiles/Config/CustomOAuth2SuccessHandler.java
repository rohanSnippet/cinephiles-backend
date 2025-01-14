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

    private UserService userService;

    public CustomOAuth2SuccessHandler(UserService userService) {

        this.userService = userService;
    }

    // This is the method that will be called when authentication is successful
    @Override

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Redirect to the frontend
        response.sendRedirect("http://localhost:5173/login");
    }
}

