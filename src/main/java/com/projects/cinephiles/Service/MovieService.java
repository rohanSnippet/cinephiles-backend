package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.RestPageImpl;
import com.projects.cinephiles.Repo.CrewMemberRepo;
import com.projects.cinephiles.Repo.MovieRepo;
import com.projects.cinephiles.Repo.TheatreRepo;
import com.projects.cinephiles.models.CrewMember;
import com.projects.cinephiles.models.Movie;
import com.projects.cinephiles.models.Trailers;
import io.netty.util.concurrent.CompleteFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private CrewMemberRepo crewMemberRepo;

    @Autowired
    private TheatreRepo theatreRepo;

    @Cacheable(value = "featuredMovies", key = "#region")
    public List<Movie> getFeaturedMoviesByRegion(String region) {
        return movieRepo.findFeaturedByRegionOrGlobal(region);
    }

    @Transactional
    @CacheEvict(value = "featuredMovies", key = "#region")
    public void updateFeaturedMoviesForRegion(String region, List<Long> movieIds) {
        // 1. Remove this specific region from previously featured movies
        List<Movie> currentlyFeatured = movieRepo.findFeaturedByExactRegion(region);
        for (Movie movie : currentlyFeatured) {
            movie.getFeaturedRegions().remove(region);
            movieRepo.save(movie);
        }

        // 2. Add the region to the newly selected movies
        if (movieIds != null && !movieIds.isEmpty()) {
            List<Movie> newlyFeatured = movieRepo.findAllById(movieIds);
            for (Movie movie : newlyFeatured) {
                movie.getFeaturedRegions().add(region);
                movieRepo.save(movie);
            }
        }
    }

    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieRepo.findAll();
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", key = "'upcoming'"),
            @CacheEvict(value = "upcomingMoviesPaginated", allEntries = true),
            @CacheEvict(value = "featuredMovies", allEntries = true)
    })
    public Movie saveMovie(Movie movie) {
        List<CrewMember> crewMembers = movie.getCrew();

        if (crewMembers != null) {
            for (CrewMember crewMember : crewMembers) {
                // Set the movie reference on each crew member
                crewMember.setMovie(movie);
            }
        }


    List<Trailers> trailers = movie.getTrailers();
    if (trailers != null) {
        for (Trailers trailer : trailers) {
            trailer.setMovie(movie);
        }
    }
        // Save the movie entity, which will cascade and save the associated crew members
        return movieRepo.save(movie);
    }

    // Method to get a specific movie by its ID
    public Optional<Movie> getMovie(Long id) {
        return movieRepo.findById(id);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", key="'upcoming'"),
            @CacheEvict(value = "upcomingMoviesPaginated", allEntries = true),
            @CacheEvict(value = "featuredMovies", allEntries = true)
    })
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

    public List<Movie> getMoviesByCity(List<String> cities) {
        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // Fetch movies based on both date and time constraints
        return movieRepo.findMoviesByCitiesAndDateAndTime(cities, todayDate, currentTime);
    }

    @Transactional
    @Cacheable(value = "movies", key = "'upcoming'")
    public List<Movie> getUpcomingMovies() {
        System.out.println("CACHE MISS: Fetching upcoming movies from Database!");

        List<Movie> movies = movieRepo.findAll();
        LocalDate today = LocalDate.now();

        // Filter and collect upcoming movies
        return movies.stream()
                .filter(movie -> movie.getReleaseDate() != null && movie.getReleaseDate().compareTo(today) > 0)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "upcomingMoviesPaginated", key = "#page + '-' + #size + '-' + #sortBy + '-' + #direction")
    public RestPageImpl<Movie> getUpcomingMovies(int page, int size, String sortBy, String direction){
        System.out.println("CACHE MISS: Fetching Paginated Data from DB for Page: " + page);

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending():
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDate today = LocalDate.now();

        // 1. Get the raw page from the database
        Page<Movie> rawPage = movieRepo.findByReleaseDateAfter(today, pageable);

        // 2. Wrap it in the serializable class and return it
        return new RestPageImpl<>(rawPage);
    }

    @Caching(evict = {
            @CacheEvict(value = "movies", key = "'upcoming'"),
            @CacheEvict(value = "upcomingMoviesPaginated", allEntries = true),
            @CacheEvict(value = "featuredMovies", allEntries = true)
    })
    public ResponseEntity<String> editMovie(Long id, Movie updatedMovie) {
        Optional<Movie> optionalMovie = movieRepo.findById(id);

        if (optionalMovie.isEmpty()) {
            return new ResponseEntity<>("Movie Not found", HttpStatus.NOT_FOUND);
        }

        Movie existingMovie = optionalMovie.get();

        // Update basic fields
        existingMovie.setTitle(updatedMovie.getTitle());
        existingMovie.setRuntime(updatedMovie.getRuntime());
        existingMovie.setDescription(updatedMovie.getDescription());
        existingMovie.setCertification(updatedMovie.getCertification());
        existingMovie.setRatings(updatedMovie.getRatings());
        existingMovie.setVotes(updatedMovie.getVotes());
        existingMovie.setLikes(updatedMovie.getLikes());
        existingMovie.setPoster(updatedMovie.getPoster());
        existingMovie.setBanner(updatedMovie.getBanner());
        existingMovie.setReleaseDate(updatedMovie.getReleaseDate());
        existingMovie.setBookingOpen(updatedMovie.getBookingOpen());
        existingMovie.setPromoted(updatedMovie.getPromoted());

        // Update genres, languages, and formats (clear and re-add elements)
        existingMovie.getGenre().clear();
        existingMovie.getGenre().addAll(updatedMovie.getGenre());

        existingMovie.getLanguages().clear();
        existingMovie.getLanguages().addAll(updatedMovie.getLanguages());

        existingMovie.getFormats().clear();
        existingMovie.getFormats().addAll(updatedMovie.getFormats());

        // Update trailers and cast
        existingMovie.getTrailers().clear();
        updatedMovie.getTrailers().forEach(trailer ->{
            trailer.setMovie(existingMovie);
            existingMovie.getTrailers().add(trailer);
        });

        existingMovie.getCast().clear();
        existingMovie.getCast().putAll(updatedMovie.getCast());

        // Update the crew members list
        existingMovie.getCrew().clear(); // Clear existing crew list
        updatedMovie.getCrew().forEach(crewMember -> {
            crewMember.setMovie(existingMovie); // Reassign movie reference
            existingMovie.getCrew().add(crewMember); // Add to existing crew list
        });

        // Save the updated movie instance
        movieRepo.save(existingMovie);
        return new ResponseEntity<>("Movie Data Updated", HttpStatus.OK);
    }

    public List<Movie> searchMovies(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return movieRepo.findByTitleContainingIgnoreCase(query, pageable);
    }

    //bulk upload movies data
//    public List<Movie> saveAllMovies(List<Movie> movies){
//        if(movies == null || movies.isEmpty()) return movies;
//
//        for(Movie movie: movies){
//
//            // Set the movie reference on each crew member
//            List<CrewMember> crewMembers = movie.getCrew();
//            if (crewMembers != null) {
//                for (CrewMember crewMember : crewMembers) {
//                    crewMember.setMovie(movie);
//                }
//            }
//
//            // Set the movie reference on each trailer
//            List<Trailers> trailers = movie.getTrailers();
//            if (trailers != null) {
//                for (Trailers trailer : trailers) {
//                    trailer.setMovie(movie);
//                }
//            }
//        }
//      return movieRepo.saveAll(movies);
//    }

    @Async
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "movies", key = "'upcoming'"),
            @CacheEvict(value = "upcomingMoviesPaginated", allEntries = true),
            @CacheEvict(value = "featuredMovies", allEntries = true)
    })
    public CompletableFuture<Void> saveMoviesInBackground(List<Movie> movies){
        if (movies == null || movies.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        for (Movie movie : movies) {
            // Set relationships to avoid null references
            if (movie.getCrew() != null) {
                movie.getCrew().forEach(crew -> crew.setMovie(movie));
            }
            if (movie.getTrailers() != null) {
                movie.getTrailers().forEach(trailer -> trailer.setMovie(movie));
            }
        }

        // Save all movies in a single batch
        movieRepo.saveAll(movies);
        System.out.println("Background task: Successfully saved " + movies.size() + " movies.");
        return CompletableFuture.completedFuture(null);
    }

    // Inside MovieService.java

    public Page<Movie> getMoviesPaginated(int page, int size, String sortBy, String direction, String search) {
        // Build the sorting object
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        // Build the pagination request
        Pageable pageable = PageRequest.of(page, size, sort);

        // Call the new searchBy... method!
        if (search != null && !search.trim().isEmpty()) {
            return movieRepo.searchByTitleContainingIgnoreCase(search, pageable);
        } else {
            return movieRepo.findAll(pageable);
        }
    }

}
