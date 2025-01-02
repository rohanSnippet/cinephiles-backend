package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.OrderRepo;
import com.projects.cinephiles.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    public Order getOrderByUser(String username) {
        Order opOrder = orderRepo.findByUsername(username);
        return opOrder;
    }
}
