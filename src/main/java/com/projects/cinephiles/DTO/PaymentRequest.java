package com.projects.cinephiles.DTO;

import lombok.Data;
import java.util.List;

@Data
public class PaymentRequest {
    private String username;         // Logged-in user ID
        // Movie identifier
    private Long showId;
    private Double amount;// Show identifier
    private List<String> seatIds;  // Seats selected
}

