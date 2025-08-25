package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
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

    @Column(name = "bookingID")
    private String bookingID;

    private String tierName;
    private String seatsIds;
    @Column(name = "totalAmount")
    private Double totalAmount; //send
    @Column(name = "baseAmount")
    private Double baseAmount;
    private Double cgst;
    private Double sgst;
    @Column(name = "theatre")
    private Long theatreId;
    @Column(name = "user_email")
    private String userEmail; //send
    @Column(name = "owner")
    private Long owner; //send
}
