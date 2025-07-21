package com.projects.cinephiles.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class PaymentOrder {

    @Id
    private String orderId;
    private Long userId;
    private Long movieId;
    private Long showId;
    private String seatIds;
    private String tierName;
    private Double amount;
    private Double cgst;
    private Double sgst;
    private String status; // CREATED, PAID, FAILED
    private LocalDateTime createdAt;

}
