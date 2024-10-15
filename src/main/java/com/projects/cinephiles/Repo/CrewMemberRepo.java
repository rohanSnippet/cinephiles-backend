package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberRepo extends JpaRepository<CrewMember, Long> {
}
