package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    Order findByUsername(String username);
}
