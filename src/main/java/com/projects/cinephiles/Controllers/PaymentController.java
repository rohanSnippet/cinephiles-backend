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

    @PostMapping("/webhook")
    public ResponseEntity<String> cashfreeWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "x-webhook-signature", required = false) String signature,
            @RequestHeader(value = "x-webhook-timestamp", required = false) String timestamp) {

        System.out.println("/webhook received ......");

        // Let the service layer verify and process it
        paymentService.handleWebhook(rawPayload, signature, timestamp);

        // Cashfree expects an HTTP 200 OK to know you received it successfully
        return ResponseEntity.ok("OK");
    }
}

