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
@Table(name = "crew")
public class CrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection
    @CollectionTable(name = "crew_roles", joinColumns = @JoinColumn(name = "crew_member_id"))
    @Column(name = "role")
    private List<String> roles;  // Change this to List<String>

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private Movie movie;

}
