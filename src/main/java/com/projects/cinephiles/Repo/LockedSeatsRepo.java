package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.LockedSeats;
import com.projects.cinephiles.models.Show;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LockedSeatsRepo extends JpaRepository<LockedSeats,Long> {


    Optional<LockedSeats> findByShowIdAndUserEmail(Long showId, String userEmail);


    List<LockedSeats> findByExpirationTimeBefore(LocalDateTime dateTime);
    //@Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Query("SELECT l FROM LockedSeats l WHERE l.show = :show AND l.expirationTime = :expirationTime > :now")
   // List<LockedSeats> findActiveLockedSeatsForShow(Show show, LocalDateTime expirationTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<LockedSeats> findByShowAndExpirationTimeAfter(Show show, LocalDateTime expirationTime);
}
