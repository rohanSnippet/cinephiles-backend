package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepo extends JpaRepository<Movie,Long> {
}
