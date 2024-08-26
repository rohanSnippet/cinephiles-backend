package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "owners")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    @JsonIgnore
    private User user;

    @OneToMany
    @JsonIgnore
    private List<Theatre> theatres;
    private Float revenue;
    // Add other fields specific to Owners
}

