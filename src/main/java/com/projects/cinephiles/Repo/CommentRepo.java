package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepo extends JpaRepository<Comment, Long> {

    // Fetch top-level comments for a movie (paginated)
    Page<Comment> findByMovieIdAndParentIsNullOrderByCreatedAtDesc(Long movieId, Pageable pageable);

    // Fetch replies for a specific parent comment
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    // Efficient way to get a reply count for the UI
    long countByParentId(Long parentId);
}
