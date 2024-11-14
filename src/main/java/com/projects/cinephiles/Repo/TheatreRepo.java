package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepo extends JpaRepository<Theatre,Long> {

   @Query("SELECT t FROM Theatre t WHERE t.city IN :cities AND t.isActive = true")
List<Theatre> findByCity(@Param("cities") List<String> cities);
}
