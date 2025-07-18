package com.projects.cinephiles.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
    @JsonIgnore
    private User user;
    private Long bookingId;
    @Column(name = "username")
    private String username;
    private String status;
    private String movie;

    @Column(name = "show_id")
    private String poster;
    @Column(name = "show_data")
    private Long showId;
    private String screenName;
    private Long theatre;
    @Column(name = "seats_booked")
    private String seats;
    private Double totalAmount;
    private LocalTime bookingTime;
    private LocalDate bookingDate;
    private Boolean isCanceled=false;
}
