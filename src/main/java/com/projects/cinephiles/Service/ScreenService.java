package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.ScreenRepository;
import com.projects.cinephiles.models.Screen;
import com.projects.cinephiles.models.Seat;
import com.projects.cinephiles.models.Tier;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScreenService {

    @Autowired
    private ScreenRepository screenRepository;

   // public Screen saveScreen(Screen screen) {
   //     return screenRepository.save(screen);
  //  }
    @Transactional    public Screen saveScreen(Screen screen) {
        // Ensure tiers and seats are linked properly
        for (Tier tier : screen.getTiers()) {
            tier.setScreen(screen);
            for (Seat seat : tier.getSeats()) {
                seat.setTier(tier);
            }
        }
        return screenRepository.save(screen);
    }

    public Screen getScreen(Long id) {
        return screenRepository.findById(id).orElse(null);
    }


    public Screen updateScreen(Long id, Screen updatedScreen) {
        Screen existingScreen = screenRepository.findById(id).orElse(null);
        if (existingScreen != null) {
            existingScreen.setName(updatedScreen.getName());
            existingScreen.setTiers(updatedScreen.getTiers()); // Update tiers and seats
            return screenRepository.save(existingScreen);
        }
        return null;
    }

    public void deleteScreen(Long id) {
        screenRepository.deleteById(id);
    }

    public List<Screen> getAllScreens() {
       return screenRepository.findAll();
    }

}

