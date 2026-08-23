package com.projects.cinephiles.Service;

import com.projects.cinephiles.Controllers.TrendingStreamController;
import com.projects.cinephiles.DTO.TrendingMovieDTO;
import com.projects.cinephiles.models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrendingScheduler {

    @Autowired
    private MovieService movieService;

    @Autowired
    private TrendingStreamController trendingStreamController;

    // Cache the last broadcasted state to prevent redundant network I/O
    private List<Long> lastBroadcastedMovieIds = null;

    // Executes every 60,000 milliseconds (60 seconds)
    @Scheduled(fixedRate = 60000)
    public void debounceTrendingBroadcast() {
        // 1. Calculate the top 5 movies once
        List<TrendingMovieDTO> latestTrending = movieService.getTrendingMovies("24h");

        if (latestTrending == null || latestTrending.isEmpty()) {
            return;
        }

        // 2. Extract IDs to compare the new state against the old state
        List<Long> currentTrendingIds = latestTrending.stream()
                .map(TrendingMovieDTO::getId)
                .collect(Collectors.toList());

        // 3. Compare with the last broadcasted state
        if (!currentTrendingIds.equals(lastBroadcastedMovieIds)) {
            // 4. State has changed: Broadcast the update and save the new state
            trendingStreamController.broadcastTrendingUpdate(latestTrending);
            lastBroadcastedMovieIds = currentTrendingIds;

            System.out.println("Trending leaderboard updated and broadcasted via Scheduler.");
        }
    }
}
