package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "theatres")
public class Theatre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String address;

    private String contact;
    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Screen> screens;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private Owner owner;
}
