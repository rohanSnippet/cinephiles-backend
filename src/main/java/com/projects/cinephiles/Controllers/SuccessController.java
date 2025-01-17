package com.projects.cinephiles.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SuccessController {
    @GetMapping("/home")
    public String home() {
        return "Welcome to Home!";
    }
}