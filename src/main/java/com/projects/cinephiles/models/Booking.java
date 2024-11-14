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
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "show_id")
    @JsonIgnore
    private Show show;

    private String seatsIds;
    private Double totalAmount; //send
    @Column(name = "theatre")
    private Long theatreId;
    @Column(name = "user")
    private String user; //send
    @Column(name = "owner")
    private Long owner; //send
}
