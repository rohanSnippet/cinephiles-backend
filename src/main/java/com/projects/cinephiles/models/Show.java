package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_date", nullable = false)
    private String showDate;

    @Column(name = "start_time", nullable = false)
    private String start;

    @Column(name="runtime",nullable = false)
    private int runtime;

    @Column(name = "end_time", nullable = false)
    private String end;

    @Column(name = "movie_title", nullable = false)
    private String title;

    @Column(name = "banner", nullable = false)
    private String Banner;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnore
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "screen_id", nullable = false)
    @JsonIgnore
    private Screen screen;

    @Column(name="movie_format")
    private String format;

    @ElementCollection
    @CollectionTable(name = "blocked_seats", joinColumns = @JoinColumn(name = "show_id"))
    @Column(name = "seat_number")
    private List<String> blocked;

    @ElementCollection
    @CollectionTable(name = "booked_seats", joinColumns = @JoinColumn(name = "show_id"))
    @Column(name = "seat_number")
    private List<String> booked;

    @ElementCollection
    @CollectionTable(name = "tier_price", joinColumns = @JoinColumn(name = "show_id"))
    @MapKeyColumn(name = "tier_name")
    @Column(name = "price")
    private Map<String, Integer> price;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Booking> bookings;

    @ManyToOne
    @JoinColumn(name = "theatre_id")
    @JsonIgnore
    private Theatre theatre;

    // Add soft delete flag if needed
    @Column(name = "is_active")
    private boolean isActive = true;

    // Method to perform soft delete
    public void delete() {
        this.isActive = false;
    }
}


