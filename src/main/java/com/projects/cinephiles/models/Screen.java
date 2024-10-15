package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "screens")
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sname;

    @ManyToOne
    @JoinColumn(name = "theatre_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Theatre theatre;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Show> shows;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)

    private List<Tier> tiers;

    // Add soft delete flag if needed
    private boolean isActive = true;
}

