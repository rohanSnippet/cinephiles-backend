package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.LockedSeats;
import com.projects.cinephiles.models.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LockedSeatsRepo extends JpaRepository<LockedSeats,Long> {

    Optional<LockedSeats> findByShowIdAndUser(Long showId, String username);

    List<LockedSeats> findByShowAndExpirationTimeAfter(Show show, LocalDateTime expirationTime);}
