package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Movie;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepo extends JpaRepository<Movie,Long> {


    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.shows s " +
            "WHERE s.theatre.city IN :cities " +
            "AND (s.showDate > :todayDate " +
            "OR (s.showDate = :todayDate AND s.start > :currentTime))")
    List<Movie> findMoviesByCitiesAndDateAndTime(
            @Param("cities") List<String> cities,
            @Param("todayDate") String todayDate,
            @Param("currentTime") String currentTime);


    List<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
