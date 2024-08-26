package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.ScreenService;
import com.projects.cinephiles.models.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owner/screens")
public class ScreenController {

    @Autowired
    private ScreenService screenService;

    @PostMapping
    public ResponseEntity<Screen> createScreen(@RequestBody Screen screen) {
        return ResponseEntity.ok(screenService.saveScreen(screen));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Screen> getScreen(@PathVariable Long id) {
        return ResponseEntity.ok(screenService.getScreen(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('THEATRE_OWNER','ADMIN')")
    public ResponseEntity<List<Screen>> getAllScreens() {
        return ResponseEntity.ok(screenService.getAllScreens());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('THEATRE_OWNER')")
    public ResponseEntity<Screen> updateScreen(@PathVariable Long id, @RequestBody Screen updatedScreen) {
        return ResponseEntity.ok(screenService.updateScreen(id, updatedScreen));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('THEATRE_OWNER')")
    public ResponseEntity<Void> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity.noContent().build();
    }
}
