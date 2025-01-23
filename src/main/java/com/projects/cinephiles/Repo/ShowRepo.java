package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowRepo extends JpaRepository<Show,Long> {
    Show save(Show show);

//    @Query("SELECT s FROM Show s JOIN s.theatre t WHERE s.movie.id = :movieId AND t.city IN :cities AND s.isActive = true")
//    List<Show> findActiveShowsByMovieAndCity(@Param("movieId") Long movieId, @Param("cities") List<String> cities);

    @Query("SELECT s FROM Show s JOIN FETCH s.theatre t " +
            "WHERE s.movie.id = :movieId AND t.city IN :cities " +
            "AND s.isActive = true")
    List<Show> findActiveShowsByMovieAndCity(@Param("movieId") Long movieId, @Param("cities") List<String> cities);

        @Query("SELECT s FROM Show s WHERE s.isActive = true")
        List<Show> findActiveShows();



}
