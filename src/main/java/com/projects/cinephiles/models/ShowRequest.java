package com.projects.cinephiles.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowRequest {


    private String showDate;

    private String start;

    private String end;

    private Long movieId;

    private Long screenId;

    private String format;

    private List<String> blocked;

    @ElementCollection
    @CollectionTable(name = "tier_price", joinColumns = @JoinColumn(name = "showRequest_id"))
    @MapKeyColumn(name = "tier_name")
    @Column(name = "price")
    private Map<String, Integer> price;

    private Long theatreId;

}
