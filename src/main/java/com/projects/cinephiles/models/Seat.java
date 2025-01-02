package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projects.cinephiles.Enum.SeatStatus;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @ManyToOne
    @JoinColumn(name = "tier_id", nullable = false)
    @JsonIgnore
    private Tier tier;
}
