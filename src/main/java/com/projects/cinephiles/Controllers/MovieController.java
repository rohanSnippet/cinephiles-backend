package com.projects.cinephiles.Controllers;


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


    @GetMapping("/all-movies")
    public ResponseEntity<List<Movie>> getAllMovies(){
        return movieService.getAllMovies();
    }

    @GetMapping("/upcoming-movies")
    public ResponseEntity<List<Movie>> getUpcomingMovies(){
        return movieService.getUpcomingMovies();
    }

    @PostMapping("/add-movie")
    @PreAuthorize(("hasRole('ADMIN')"))
    public Movie addMovie(@RequestBody Movie movie){
        return movieService.saveMovie(movie);
    }

    @PostMapping("/get-movie/{id}")
    public Optional<Movie> getMovie(@PathVariable Long id ){
        return movieService.getMovie(id);
    }

    @DeleteMapping("/delete-movie/{id}")
    public ResponseEntity<String> deleteMovie(@PathVariable Long id){
        return movieService.deleteMovie(id);
    }
    @PutMapping("/edit-movie/{id}")
    public ResponseEntity<String> editMovie(@PathVariable Long id,@RequestBody Movie movie){

        return movieService.editMovie(id,movie);
    }

    @GetMapping("/by-city")
    public ResponseEntity<List<Movie>> getMoviesByCities(@RequestParam List<String> cities) {
        List<Movie> movies = movieService.getMoviesByCity(cities);
        return ResponseEntity.ok(movies);
    }


}
