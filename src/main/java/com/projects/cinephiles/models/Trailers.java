package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "trailers")
public class Trailers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;

    @ElementCollection
    @CollectionTable(name = "trailer_url", joinColumns = @JoinColumn(name = "trailer_url_id"))
    @Column(name = "url")
    private List<String> trailerUrl;  // Change this to List<String>

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private Movie movie;

}
