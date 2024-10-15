package com.projects.cinephiles.Controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/owner")
@PreAuthorize("hasRole('THEATRE_OWNER')")
public class OwnerController {

}
