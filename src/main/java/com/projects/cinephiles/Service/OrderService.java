package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.OrderRepo;
import com.projects.cinephiles.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    public ResponseEntity<List<Order>> findByUsername(String username) {
        List<Order> orders = orderRepo.findByUsername(username);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
}
