package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projects.cinephiles.Enum.Certification;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer runtime;
    private String description;

    @Enumerated(EnumType.STRING)
    private Certification certification;

    @ElementCollection
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    private List<String> genre;

    @ElementCollection
    @CollectionTable(name = "movie_languages", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "language")
    private List<String> languages;

    @ElementCollection
    @CollectionTable(name = "movie_formats", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "formats")
    private List<String> formats;

    private Double ratings;
    private Integer votes;
    private Integer likes;

    @ElementCollection
    @CollectionTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"))
    @MapKeyColumn(name = "actor_name")
    @Column(name = "role")
    private Map<String, String> cast;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)  // Cascade and orphan removal
    private List<CrewMember> crew;

    private String poster;
    private String banner;

    @ElementCollection
    @CollectionTable(name = "movie_trailers", joinColumns = @JoinColumn(name = "movie_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "trailer_link")
    private Map<String, String> trailers;

    private LocalDate releaseDate;
    private Boolean bookingOpen;
    private Boolean promoted;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)  // Cascade and orphan removal
    @JsonIgnore
    private List<Show> shows;
}
