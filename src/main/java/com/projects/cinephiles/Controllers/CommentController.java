package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.DTO.CommentRequestDto;
import com.projects.cinephiles.DTO.CommentResponseDto;
import com.projects.cinephiles.Service.CommentService;
import com.projects.cinephiles.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto request, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(commentService.addComment(request, currentUser));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.getMovieComments(movieId, PageRequest.of(page, size)));
    }

    @GetMapping("/{parentId}/replies")
    public ResponseEntity<List<CommentResponseDto>> getReplies(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getReplies(parentId));
    }
}
