package com.projects.cinephiles.DTO;

import lombok.Data;

@Data
public class CommentRequestDto {
    private Long movieId;
    private String content;
    private Long parentId; // Null if it's a top-level comment
    private String repliedToUsername;
}
