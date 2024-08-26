package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TheatreRepo extends JpaRepository<Theatre,Long> {

}
