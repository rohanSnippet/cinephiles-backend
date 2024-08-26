package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<Admin,Long> {
}
