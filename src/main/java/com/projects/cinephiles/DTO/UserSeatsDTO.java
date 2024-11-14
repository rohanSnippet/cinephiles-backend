package com.projects.cinephiles.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSeatsDTO {
    private Long showId;
    private List<String> seatsId;
    private Double price;
    private String user;

}

