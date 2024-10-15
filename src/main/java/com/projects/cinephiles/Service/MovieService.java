package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.CrewMemberRepo;
import com.projects.cinephiles.Repo.MovieRepo;
import com.projects.cinephiles.models.CrewMember;
import com.projects.cinephiles.models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private CrewMemberRepo crewMemberRepo;

    // Method to get all movies
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieRepo.findAll();
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    // Method to add a new movie
    public Movie addMovie(Movie movie) {

        if (movie.getCrew() != null) {
            for (CrewMember crewMember : movie.getCrew()) {
                crewMember.setMovie(movie);  // Set the movie reference for each crew member
            }
        }

        Movie savedMovie = movieRepo.save(movie);

        if (movie.getCrew() != null) {
            for (CrewMember crewMember : movie.getCrew()) {
                crewMember.setMovie(savedMovie);  // Set the movie reference for each crew member
                crewMemberRepo.save(crewMember);  // Save the crew member with the movie reference
            }
        }
        return movieRepo.save(movie);
    }

    // Method to get a specific movie by its ID
    public Optional<Movie> getMovie(Long id) {
        return movieRepo.findById(id);
    }


    @Transactional
    public ResponseEntity<String> deleteMovie(Long id) {
        // Check if the movie exists in the database
        Optional<Movie> movieOptional = movieRepo.findById(id);

        // If the movie is present, delete it
        if (movieOptional.isPresent()) {
            movieRepo.deleteById(id);  // This will automatically delete associated crew members because of cascade settings
            return new ResponseEntity<>("Movie deleted successfully", HttpStatus.OK);
        } else {
            // If the movie does not exist, return a 404 Not Found response
            return new ResponseEntity<>("Movie not found", HttpStatus.NOT_FOUND);
        }
    }

}
