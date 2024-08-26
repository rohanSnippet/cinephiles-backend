package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/owner")
@PreAuthorize("hasRole('THEATRE_OWNER')")
public class OwnerController {

    @Autowired
    private UserService userService;
}
