package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierRepo extends JpaRepository<Tier,Long> {
}
