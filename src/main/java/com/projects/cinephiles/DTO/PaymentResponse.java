package com.projects.cinephiles.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String paymentSessionId;
    private String orderId;
    private String returnUrl;
}
