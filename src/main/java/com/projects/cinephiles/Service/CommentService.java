package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.CommentRequestDto;
import com.projects.cinephiles.DTO.CommentResponseDto;
import com.projects.cinephiles.Repo.CommentRepo;
import com.projects.cinephiles.models.Comment;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepo commentRepository;

    @Transactional
    public CommentResponseDto addComment(CommentRequestDto request, User currentUser) {
        String email = currentUser.getUsername();
        String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
        String profile = currentUser.getProfile();

        Comment comment = new Comment();
        comment.setMovieId(request.getMovieId());
        comment.setContent(request.getContent());
        comment.setAuthorUsername(email);
        comment.setFullName(fullName);
        comment.setProfile(profile);

        if (request.getParentId() != null) {
            // We ensure the parent is always the TOP LEVEL comment
            Comment topLevelParent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Thread not found"));

            comment.setParent(topLevelParent);
            comment.setRepliedToUsername(request.getRepliedToUsername());
        }

        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }

    public Page<CommentResponseDto> getMovieComments(Long movieId, Pageable pageable) {
        return commentRepository.findByMovieIdAndParentIsNullOrderByCreatedAtDesc(movieId, pageable)
                .map(this::mapToDto);
    }

    public List<CommentResponseDto> getReplies(Long parentId) {
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CommentResponseDto mapToDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setAuthorUsername(comment.getAuthorUsername());
        dto.setFullName(comment.getFullName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpvotes(comment.getUpvotes());
        dto.setProfile(comment.getProfile());
        dto.setReplyCount(commentRepository.countByParentId(comment.getId()));
        return dto;
    }
}
