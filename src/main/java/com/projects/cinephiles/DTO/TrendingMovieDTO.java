package com.projects.cinephiles.DTO;

import lombok.Data;
import java.util.List;

@Data
public class TrendingMovieDTO {
    private Long id;
    private String title;
    private String poster;
    private List<String> genre;
}
