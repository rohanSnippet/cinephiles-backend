package com.projects.cinephiles.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locked_seats")
public class LockedSeats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "locked_seat_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "show_id", nullable = false) // Make sure this column matches the foreign key in the DB
    private Show show;

    @ElementCollection
    @CollectionTable(name = "locked_seats_details", joinColumns = @JoinColumn(name = "locked_seat_id"))
    @Column(name = "seats")
    private List<String> seatsId;
    private String tierName;
    private Double price;
    private Double cgst;
    private Double sgst;
    private LocalDateTime expirationTime;
    @Column(name = "user_email")
    private String userEmail;

}
