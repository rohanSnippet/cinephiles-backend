package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.DTO.ProfilesUrl;
import com.projects.cinephiles.Service.ScrapeService;
import com.projects.cinephiles.models.CrewMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/actor")
public class ActorController {

    @Autowired
    private ScrapeService scrapeService;

    // --- NEW: Single Fetch Endpoint for Incremental UI Loading ---
    @GetMapping("/scrape-single")
    public ResponseEntity<Map<String, String>> scrapeSingle(
            @RequestParam String name,
            @RequestParam boolean isCrew) {

        // Calls the new single scraping method and returns { "url": "..." }
        Map<String, String> response = scrapeService.scrapeSinglePerson(name, isCrew);
        return ResponseEntity.ok(response);
    }

    // --- OLD: Bulk Fetch Endpoints (Retained just in case, but updated to use instance methods) ---
    @PostMapping("/scrape")
    public ProfilesUrl scrapeActor(@RequestBody Map<String, String> cast) {
        // Changed ScrapeService.scrapeActor to scrapeService.scrapeActor
        return scrapeService.scrapeActor(cast);
    }

    @PostMapping("/scrape-crew")
    public ProfilesUrl scrapeCrew(@RequestBody List<CrewMember> crews) {
        // Changed ScrapeService.scrapeCrew to scrapeService.scrapeCrew
        return scrapeService.scrapeCrew(crews);
    }
}