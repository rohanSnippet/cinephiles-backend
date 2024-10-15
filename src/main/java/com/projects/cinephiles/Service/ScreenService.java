package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.ScreenRepository;
import com.projects.cinephiles.Repo.TheatreRepo;
import com.projects.cinephiles.Repo.TierRepo;
import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.models.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;
    @Autowired
    private TheatreRepo theatreRepo;
    @Autowired
    private UserRepo userRepo;

    @Transactional
    public Screen saveScreen(Long tId,Screen screen) {
        // Ensure tiers and seats are linked properly
        Optional<Theatre> optionalTheatre = theatreRepo.findById(tId);

        // Ensure that the theatre exists
        if (optionalTheatre.isEmpty()) {
            throw new IllegalArgumentException("Theatre with ID " + tId + " not found.");
        }

        // Set the theatre for the screen
        Theatre theatre = optionalTheatre.get();
        screen.setTheatre(theatre);

        // Link tiers and seats properly to the screen
        if (screen.getTiers() != null) {
            for (Tier tier : screen.getTiers()) {
                tier.setScreen(screen); // Set the screen for each tier

                if (tier.getSeats() != null) {
                    for (Seat seat : tier.getSeats()) {
                        seat.setTier(tier); // Set the tier for each seat
                    }
                }
            }
        }
        return screenRepository.save(screen);
    }

    public Screen getScreen(Long id) {
        return screenRepository.findById(id).orElse(null);
    }


    @Autowired
    private TierRepo tierRepo;

    @Transactional
    public Screen updateScreen(Long id, Screen updatedScreen) {
        Screen existingScreen = screenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Screen with ID " + id + " not found."));
          if(existingScreen != null){
              existingScreen.setSname(updatedScreen.getSname());
              existingScreen.setTiers(updatedScreen.getTiers());
          }else{
              System.out.println("NO SUCH SCREEN");
          }
        return screenRepository.save(existingScreen);
    }




    public void deleteScreen(Long id) {
        screenRepository.deleteById(id);
    }

    public List<Screen> getAllScreens() {
       return screenRepository.findAll();
    }

    public List<Screen> getScreenByTheatreId(Long theatreId) {
        Optional<Theatre> opTheatre = theatreRepo.findById(theatreId);
        if(opTheatre.isPresent()){
            Theatre theatre = opTheatre.get();
            return theatre.getScreens();
        }else {
            // Handle case when the theatre with the given ID is not found
            throw new IllegalArgumentException("Theatre with ID " + theatreId + " not found.");
        }

    }

    public List<Screen> getScreenByUsername(String username) {
        User user = userRepo.getUserByUsername(username);
        if (user == null || user.getOwner() == null) {
            System.out.println("User or owner not found for username: " + username);
            return Collections.emptyList();
        }

        Owner owner = user.getOwner();
        System.out.println("Owner found: " + owner.getId() + " with theatres: " + owner.getTheatres().size());

        List<Screen> screens = owner.getTheatres().stream()
                .filter(theatre -> theatre.getScreens() != null)
                .flatMap(theatre -> {
                    System.out.println("Theatre: " + theatre.getId() + " has screens: " + theatre.getScreens().size());
                    return theatre.getScreens().stream();
                })
                .collect(Collectors.toList());

        System.out.println("Total screens found: " + screens.size());
        return screens;
    }

}

