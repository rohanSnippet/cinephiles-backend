package com.projects.cinephiles.Config;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private String customerId;
    private String customerPhone;
    private double orderAmount;
    private List<String> seatNumbers;
}

