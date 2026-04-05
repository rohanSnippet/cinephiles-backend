package com.projects.cinephiles.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentResponseDto {
    private Long id;
    private String authorUsername;
    private String fullName;
    private String profile;
    private String content;
    private LocalDateTime createdAt;
    private int upvotes;
    private String repliedToUsername;
    private long replyCount; // Tells React whether to render a "View Replies" button
}
