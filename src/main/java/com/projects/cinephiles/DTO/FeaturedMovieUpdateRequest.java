package com.projects.cinephiles.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class FeaturedMovieUpdateRequest {
    private String region;
    private List<Long> movieIds;
}
