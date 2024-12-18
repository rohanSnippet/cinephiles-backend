package com.projects.cinephiles.DTO;

import lombok.Data;

import java.util.List;

@Data
public class LockedSeatsRequests {
    private List<String> seatsId; // List of seat IDs to be locked
    private Double price;          // Price for the locked seats
    private String user;          // User's email or identifier
    private Long showId;          // ID of the show for which seats are being locked
}