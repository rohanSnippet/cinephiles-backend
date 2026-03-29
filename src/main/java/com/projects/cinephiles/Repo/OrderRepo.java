package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Movie;
import com.projects.cinephiles.models.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByUsername(String username);

    @Query("SELECT s.movie FROM Order o JOIN Show s ON o.showId = s.id " +
            "WHERE o.isCanceled = false AND " +
            "(o.bookingDate > :startDate OR (o.bookingDate = :startDate AND o.bookingTime >= :startTime)) " +
            "GROUP BY s.movie " +
            "ORDER BY COUNT(o.id) DESC")
    List<Movie> findTrendingMovies(
            @Param("startDate") LocalDate startDate,
            @Param("startTime") LocalTime startTime,
            Pageable pageable
    );
}
