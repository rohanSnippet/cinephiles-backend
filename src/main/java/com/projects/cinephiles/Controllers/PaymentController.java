package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.DTO.PaymentRequest;
import com.projects.cinephiles.DTO.PaymentResponse;
import com.projects.cinephiles.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createOrder(@RequestBody PaymentRequest request) {
        System.out.println("/create-order called ......");
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @GetMapping("/verify/{orderId}")
    public ResponseEntity<?> verifyPayment(@PathVariable String orderId) {
        System.out.println("/verify called ......");
        return ResponseEntity.ok(paymentService.verifyPayment(orderId));
    }
}

