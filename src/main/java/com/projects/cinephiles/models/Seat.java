package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatId;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "tier_id")
    @JsonIgnore
    private Tier tier;

}
