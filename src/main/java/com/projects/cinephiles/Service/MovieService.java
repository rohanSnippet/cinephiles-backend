package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.MovieRepo;
import com.projects.cinephiles.models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepo movieRepo;

    public ResponseEntity<List<Movie>> getAllMovies() {
        return (ResponseEntity<List<Movie>>) movieRepo.findAll();
    }

    public Movie addMovie(Movie movie) {
        return movieRepo.save(movie);
    }
}
