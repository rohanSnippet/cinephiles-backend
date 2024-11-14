package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.OwnerRepo;
import com.projects.cinephiles.Repo.TheatreRepo;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.models.Theatre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TheatreService {
    @Autowired
    private TheatreRepo theatreRepo;
    @Autowired
    private OwnerRepo ownerRepo;
   @Autowired
    private UserRepo userRepo;
    public Theatre addTheatre(Theatre theatre) {
        return theatreRepo.save(theatre);
    }

    public List<Theatre> getTheatresByLocation(List<String> cities){
        return theatreRepo.findByCity(cities);
    }

    public List<Theatre> getAllTheatres() {
      return  theatreRepo.findAll();
    }

}
