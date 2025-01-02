package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.Service.OrderService;
import com.projects.cinephiles.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/getOrder/{username}")
    public ResponseEntity<Order> getOrderByUser(@PathVariable String username){
        Order order = orderService.getOrderByUser(username);
        return ResponseEntity.ok(order);
    }
}
