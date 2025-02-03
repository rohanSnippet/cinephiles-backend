package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.projects.cinephiles.Enum.ShowStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "shows")
public class Show implements Serializable {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
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
    private Map<String, Double> price;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<Booking> bookings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id")
    @JsonIgnore
    @ToString.Exclude
    private Theatre theatre;

    @Column(name = "m_Id")
   private Long mId;

    @Column(name = "t_Id")
    private Long tId;

    @Column(name = "s_Id")
    private Long sId;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude
    private List<LockedSeats> lockedSeats;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "total_seats")
    private int totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_Status")
    private ShowStatus status;

    // Method to perform soft delete
    public void delete() {
        this.isActive = false;
    }

}

