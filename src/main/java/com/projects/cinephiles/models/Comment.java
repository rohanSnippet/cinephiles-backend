package com.projects.cinephiles.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long movieId; // Links to the specific movie in Cinephiles

    @Column(nullable = false)
    private String authorUsername; // Or a @ManyToOne mapping to your User entity

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = true)
    private String profile;

    @Column(nullable = false, length = 1000)
    private String content;

    private LocalDateTime createdAt;

    // Self-referencing relationship for replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @Column(name = "replied_to_username")
    private String repliedToUsername; // Null if replying to the main thread, populated if replying to another reply

    // Optional: Only needed if you want to cascade deletes (deleting a comment deletes its replies)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    private int upvotes = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
