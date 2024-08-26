package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.OwnerRepo;
import com.projects.cinephiles.models.Owner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerService {
    @Autowired
    private OwnerRepo ownerRepo;

    public List<Owner> getAllOwners() {
        return ownerRepo.findAll();
    }
}
