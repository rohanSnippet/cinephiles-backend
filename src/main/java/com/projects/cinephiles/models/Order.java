package com.projects.cinephiles.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "show_id")
    private Show show;

    private Integer seats;
    private Double totalAmount;
    private LocalDateTime bookingTime;
    private Boolean isCanceled;

    public boolean cancelBooking() {
        if (LocalDateTime.now().isBefore(this.show.getShowTime())) {
            this.isCanceled = true;
            return true;
        }
        return false;
    }
}
