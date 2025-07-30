package com.projects.cinephiles.Config;

import com.projects.cinephiles.JwtConfig.JwtHelper;
import com.projects.cinephiles.Service.UserService;
import com.projects.cinephiles.models.User;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${frontend.return.url}")
    private String frontendUrl;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtHelper jwtHelper;

    public CustomOAuth2SuccessHandler(UserService userService) {

        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User  oAuth2User  = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User .getAttribute("email");
        System.out.println("Oauth user:  "+oAuth2User.getAttributes());
        User user = userService.getUserByUsername(email);
        if(user==null){
            user = userService.createUserFromOAuth2(oAuth2User);
        }
        // Generate JWT token
        String jwtToken = jwtHelper.generateToken(oAuth2User);
        System.out.println("jwtToken in success handler : "+jwtToken);
        request.getSession().setAttribute("access-token", jwtToken);
        response.sendRedirect(frontendUrl);
    }
}

