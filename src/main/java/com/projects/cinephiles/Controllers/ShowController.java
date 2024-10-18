package com.projects.cinephiles.Controllers;
import com.projects.cinephiles.Service.ShowService;
import com.projects.cinephiles.models.Show;
import com.projects.cinephiles.models.ShowRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/show")
public class ShowController {

    @Autowired
    ShowService showService;

    @PostMapping("/create")
    public Show createShow(@RequestBody ShowRequest showRequest){
        return showService.createShow(showRequest);
    }

    @GetMapping("/all")
    public List<Show> getShowsByTheatreAndDate(@RequestParam Long tid, @RequestParam String showDate){
        return showService.getShowsByTheatreAndDate(tid,showDate);
    }

    @GetMapping("/byScreen")
    public List<Show> getShowsByScreen(@RequestParam Long screenId, @RequestParam String showDate){
        return showService.getShowsByScreenId(screenId,showDate);
    }
    @GetMapping("/last-show")
    public Show getLastShowOfDay(@RequestParam Long screenId, @RequestParam String showDate){
        return showService.getLastShowOfDay(screenId,showDate);
    }
}
