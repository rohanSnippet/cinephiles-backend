package com.projects.cinephiles.Controllers;


import com.projects.cinephiles.DTO.RestPageImpl;
import com.projects.cinephiles.Service.MovieService;
import com.projects.cinephiles.models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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

    // In MovieController.java

    // In MovieController.java
    @GetMapping("/featured")
    public ResponseEntity<List<Movie>> getFeaturedMovies(@RequestParam(defaultValue = "GLOBAL") String region) {
        List<Movie> featured = movieService.getFeaturedMoviesByRegion(region);
        return new ResponseEntity<>(featured, HttpStatus.OK);
    }



    @GetMapping("/all-movies")
    public ResponseEntity<List<Movie>> getAllMovies(){
        return movieService.getAllMovies();
    }

    @GetMapping("/upcoming-movies")
    public ResponseEntity<List<Movie>> getUpcomingMovies(){
        List<Movie> upcomming = movieService.getUpcomingMovies();
        return new ResponseEntity<>(upcomming, HttpStatus.OK);
    }

    @GetMapping("/upcoming-page")
    public ResponseEntity<RestPageImpl<Movie>> getUpcomingMoviesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        RestPageImpl<Movie> paginatedMovies = movieService.getUpcomingMovies(page, size, sortBy, direction);
        return new ResponseEntity<>(paginatedMovies, HttpStatus.OK);
    }

    @PostMapping("/add-movie")
    @PreAuthorize("hasRole('ADMIN')")
    public Movie addMovie(@RequestBody Movie movie){
        return movieService.saveMovie(movie);
    }

    @PostMapping("/get-movie/{id}")
    public Optional<Movie> getMovie(@PathVariable Long id ){
        return movieService.getMovie(id);
    }

    @DeleteMapping("/delete-movie/{id}")
    @PreAuthorize(("hasRole('ADMIN')"))
    public ResponseEntity<String> deleteMovie(@PathVariable Long id){
        return movieService.deleteMovie(id);
    }

    @PutMapping("/edit-movie/{id}")
    @PreAuthorize(("hasRole('ADMIN')"))
    public ResponseEntity<String> editMovie(@PathVariable Long id,@RequestBody Movie movie){

        return movieService.editMovie(id,movie);
    }

    @GetMapping("/by-city")
    public ResponseEntity<List<Movie>> getMoviesByCities(@RequestParam List<String> cities) {
        List<Movie> movies = movieService.getMoviesByCity(cities);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        List<Movie> movies = movieService.searchMovies(query, limit);
        return ResponseEntity.ok(movies);
    }

    @PostMapping("/bulk-movies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addMultipleMovies(@RequestBody List<Movie> movies){
        movieService.saveMoviesInBackground(movies);
        return new ResponseEntity<>("Movie bulk upload started in background. It will be completed shortly...", HttpStatus.CREATED);
    }


}
