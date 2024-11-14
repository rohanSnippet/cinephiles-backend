package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.DTO.ProfilesUrl;
import com.projects.cinephiles.Service.ScrapeService;
import com.projects.cinephiles.models.CrewMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/actor")
public class ActorController {

    @Autowired
    private ScrapeService scrapeService;


    @PostMapping("/scrape")
    public ProfilesUrl scrapeActor(@RequestBody Map<String, String> cast) throws Exception {
        return ScrapeService.scrapeActor(cast);
    }

    @PostMapping("/scrape-crew")
    public ProfilesUrl scrapeCrew(@RequestBody List<CrewMember> crews) throws Exception {
        return ScrapeService.scrapeCrew(crews);
    }
}