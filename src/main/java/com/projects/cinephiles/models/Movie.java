package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projects.cinephiles.Enum.Certification;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private Integer runtime;

    @Column(length = 2000)
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

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrewMember> crew;

    @Column(length = 1000)
    private String poster;

    @Column(length = 1000)
    private String banner;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL , orphanRemoval = true)
    @Column(length = 1000)
    private List<Trailers> trailers;

    private LocalDate releaseDate;
    private Boolean bookingOpen;
    private Boolean promoted;

//    // In Movie.java
//    @ElementCollection
//    @CollectionTable(name = "movie_promoted_regions", joinColumns = @JoinColumn(name = "movie_id"))
//    @Column(name = "region")
//    private List<String> promotedRegions; // e.g., ["Maharashtra", "Delhi NCR", "GLOBAL"]

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_featured_regions", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "region_name")
    private Set<String> featuredRegions = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)  // Cascade and orphan removal
    @JsonIgnore
    private List<Show> shows;
}
