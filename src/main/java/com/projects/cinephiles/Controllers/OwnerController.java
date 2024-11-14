package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.OwnerService;
import com.projects.cinephiles.models.Owner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @GetMapping("/get-owners")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Owner> getAllOwners() {
        return ownerService.getAllOwners();
    }
}
