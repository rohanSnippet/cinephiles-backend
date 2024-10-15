package com.projects.cinephiles.JwtConfig;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String token = (String) authentication.getCredentials();
        String fetchedToken = token;
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); // Allow your frontend URL
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();

        // Assuming you are sending back a JSON response
        writer.write("{\"token\":\"" + fetchedToken + "\"}");
        writer.flush();
    }
}

