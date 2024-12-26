//package com.projects.cinephiles.JwtConfig;
//
//import com.projects.cinephiles.Service.UserService;
//import com.projects.cinephiles.models.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.Optional;
//
////@Component
////public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
////
////    @Override
////    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
////                                        Authentication authentication) throws IOException, ServletException {
////        String token = (String) authentication.getCredentials();
////        String fetchedToken = token;
////        response.setHeader("Access-Control-Allow-Origin", "http://localhost:5173"); // Allow your frontend URL
////        response.setContentType("application/json");
////        PrintWriter writer = response.getWriter();
////
////        // Assuming you are sending back a JSON response
////        writer.write("{\"token\":\"" + fetchedToken + "\"}");
////        writer.flush();
////    }
////}
//
//@Component
//public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
//
//    @Autowired
//    private UserService userService;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                        Authentication authentication) throws IOException, ServletException {
//        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
//        OAuth2User oauth2User = authenticationToken.getPrincipal();
//        String email = oauth2User.getAttribute("email");
//
//        // Check if the user exists in your database
//        User userOptional = userService.getUserByUsername(email);
//
//
//            // Create a new user
//            User newUser = new User();
//            newUser.setUsername(email);
//            newUser.setFirstName(oauth2User.getAttribute("login")); // GitHub login name
//            newUser.setLastName(oauth2User.getAttribute("name"));
//            userService.createUser(newUser);
//            // Your logic for logging in the newly created user
//
//
//        // Redirect to the home page or dashboard
//        response.sendRedirect("/home");
//    }
//}
