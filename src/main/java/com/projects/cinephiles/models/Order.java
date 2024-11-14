package com.projects.cinephiles.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "username")
    private String username;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    @Column(name = "seats_booked")
    private String seats;
    private Double totalAmount;
    private LocalTime bookingTime;
    private LocalDate bookingDate;
    private Boolean isCanceled=false;
}
