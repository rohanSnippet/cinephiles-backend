package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.TheatreRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TheatreRequestRepo extends JpaRepository<TheatreRequest,Long> {
}
