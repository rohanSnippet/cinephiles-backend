package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowRepo extends JpaRepository<Show,Long> {
    Show save(Show show);
}
