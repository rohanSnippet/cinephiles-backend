package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepo extends JpaRepository<Movie,Long> {

    @Query("SELECT m FROM Movie m JOIN m.featuredRegions r WHERE r = :region OR r = 'Global (Pan India)'")
    List<Movie> findFeaturedByRegionOrGlobal(@Param("region") String region);

    @Query("SELECT m FROM Movie m JOIN m.featuredRegions r WHERE r = :region")
    List<Movie> findFeaturedByExactRegion(@Param("region") String region);

    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.shows s " +
            "WHERE s.theatre.city IN :cities " +
            "AND (s.showDate > :todayDate " +
            "OR (s.showDate = :todayDate AND s.start > :currentTime))")
    List<Movie> findMoviesByCitiesAndDateAndTime(
            @Param("cities") List<String> cities,
            @Param("todayDate") String todayDate,
            @Param("currentTime") String currentTime);

    List<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> searchByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Movie> findByReleaseDateAfter(LocalDate date, Pageable pageable);

    @Query(value = """
        SELECT * FROM movies m
        WHERE 
            -- 1. Full Text Search (Matches root words in title and description)
            to_tsvector('english', m.title || ' ' || COALESCE(m.description, '')) @@ plainto_tsquery('english', :query)
            OR
            -- 2. Trigram Similarity (Catches typos like 'Btman')
            m.title % :query
            OR
            -- 3. Phonetics (Catches "sounds like" words)
            dmetaphone(m.title) = dmetaphone(:query)
        ORDER BY 
            -- Rank exact FTS matches highest, then similarity score
            ts_rank(to_tsvector('english', m.title || ' ' || COALESCE(m.description, '')), plainto_tsquery('english', :query)) DESC,
            similarity(m.title, :query) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Movie> performAdvancedSearch(@Param("query") String query, @Param("limit") int limit);
}
