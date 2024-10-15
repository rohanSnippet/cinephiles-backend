package com.projects.cinephiles.Controllers;


import com.projects.cinephiles.Service.CrewService;
import com.projects.cinephiles.Service.MovieService;
import com.projects.cinephiles.models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @Autowired
    private CrewService crewService;

    @GetMapping("/all-movies")
    @PreAuthorize("hasAnyRole('ADMIN','THEATRE_OWNER')")
    public ResponseEntity<List<Movie>> getAllMovies(){
        return movieService.getAllMovies();
    }

    @PostMapping("/add-movie")
    @PreAuthorize(("hasRole('ADMIN')"))
    public Movie addMovie(@RequestBody Movie movie){
        return movieService.addMovie(movie);
    }

    @PostMapping("/get-movie/{id}")
    public Optional<Movie> getMovie(@PathVariable Long id ){
        return movieService.getMovie(id);
    }

    @DeleteMapping("/delete-movie/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Long id){
        return movieService.deleteMovie(id);
    }
}
