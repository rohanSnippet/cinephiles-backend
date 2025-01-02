package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.DTO.OrderRequest;
import com.projects.cinephiles.Service.CashfreeService;
import com.projects.cinephiles.Service.UserService;
import com.projects.cinephiles.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private CashfreeService cashfreeService;

    @Autowired
    private UserService userService;

    @PostMapping("/create-session")
    public String createPaymentSession(@RequestBody OrderRequest orderRequest) {
        // Generate session ID using order details
        System.out.println("Order Request is : "+orderRequest.getCustomerId());

       User user = userService.getUserByUsername(orderRequest.getCustomerId());

        System.out.println(user);

        return cashfreeService.createOrderSession(
                orderRequest.getOrderId(),
                orderRequest.getOrderAmount(),
                user
        );
    }
}

