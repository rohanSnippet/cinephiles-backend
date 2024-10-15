package com.projects.cinephiles.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "theatre_requests")
public class TheatreRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="email")
    private String username;
    private String contact;
    private String tname;
    private String tlocation;
    private String state;
    private String address;
    private int tscreens;
    private String pan;
    private String accountNo;
    private String cgstNo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
