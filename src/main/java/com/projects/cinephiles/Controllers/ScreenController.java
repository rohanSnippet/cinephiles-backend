package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Repo.UserRepo;
import com.projects.cinephiles.Service.ScreenService;
import com.projects.cinephiles.models.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/create/{tId}")
    public ResponseEntity<Screen> createScreen(@PathVariable Long tId,@RequestBody Screen screen) {
        return ResponseEntity.ok(screenService.saveScreen(tId,screen));
    }

    @GetMapping("/{sid}")
    public ResponseEntity<Screen> getScreen(@PathVariable Long sid) {
        return ResponseEntity.ok(screenService.getScreen(sid));
    }
 @GetMapping("/all/{username}")
    public ResponseEntity<List<Screen>> getScreenByUsername(@PathVariable String username) {
        return ResponseEntity.ok(screenService.getScreenByUsername(username));
    }

    @GetMapping("/by-theatre/{theatreId}")
    public ResponseEntity<List<Screen>> getScreenByTheatreId(@PathVariable Long theatreId){
        return ResponseEntity.ok(screenService.getScreenByTheatreId(theatreId));
    }
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER','ADMIN')")
    public ResponseEntity<List<Screen>> getAllScreens() {
        return ResponseEntity.ok(screenService.getAllScreens());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('THEATRE_OWNER')")
    public ResponseEntity<Screen> updateScreen(@PathVariable Long id, @RequestBody Screen updatedScreen) {
        return ResponseEntity.ok(screenService.updateScreen(id, updatedScreen));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('THEATRE_OWNER')")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }
}
