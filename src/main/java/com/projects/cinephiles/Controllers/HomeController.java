package com.projects.cinephiles.Controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("/")
public class HomeController {

    @GetMapping("/hello")
    public String Hello(HttpServletRequest request){
        return "Hello user" + request.getSession().getId();
    }


}
