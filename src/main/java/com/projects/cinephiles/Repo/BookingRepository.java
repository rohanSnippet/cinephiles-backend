package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Booking;
import com.projects.cinephiles.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingID(String bookingID);

    @Query("SELECT b FROM Booking b WHERE b.show.id = :showId AND b.seatsIds LIKE concat('%', :seatId, '%')")
    Optional<Booking> findByShowIdAndSeatIdPart(@Param("showId") Long showId, @Param("seatId") String seatId);
}
