package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepo extends JpaRepository<Owner,Long> {
}
