package com.projects.cinephiles.Service;

import com.projects.cinephiles.Repo.MovieRepo;
import com.projects.cinephiles.Repo.ScreenRepository;
import com.projects.cinephiles.Repo.ShowRepo;
import com.projects.cinephiles.Repo.TheatreRepo;
import com.projects.cinephiles.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShowService {

    @Autowired
    MovieRepo movieRepo;
    @Autowired
    ScreenRepository screenRepository;
    @Autowired
    TheatreRepo theatreRepo;
    @Autowired
    ShowRepo showRepo;

    public Show createShow(ShowRequest showRequest) {
        // Fetch necessary entities from repositories
        Movie movie = movieRepo.findById(showRequest.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Screen screen = screenRepository.findById(showRequest.getScreenId())
                .orElseThrow(() -> new RuntimeException("Screen not found"));
        Theatre theatre = theatreRepo.findById(showRequest.getTheatreId())
                .orElseThrow(() -> new RuntimeException("Theatre not found"));

        // Create and save the Show entity
        Show show = new Show();
        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setRuntime(movie.getRuntime());
        show.setScreen(screen);
        show.setTitle(movie.getTitle());
        show.setBanner(movie.getBanner());
        show.setShowDate(showRequest.getShowDate());
        show.setFormat(showRequest.getFormat());
        show.setStart(showRequest.getStart());
        show.setEnd(showRequest.getEnd());
        show.setBlocked(showRequest.getBlocked());

        // Save the Show to generate the ID for the tier_price mapping
        Show savedShow = showRepo.save(show);

        // Now that the Show ID is available, set the tier prices
        savedShow.setPrice(showRequest.getPrice());

        // Save again to persist the price mapping
        return showRepo.save(savedShow);
    }


    public List<Show> getShowsByTheatreAndDate(Long tid, String showDate) {
        Optional<Theatre> optionalTheatre = theatreRepo.findById(tid);
        if(optionalTheatre.isPresent()){
            Theatre theatre = optionalTheatre.get();
            List<Show> shows = theatre.getShows();

           return shows.stream().filter(
                    show -> show.getShowDate().equals(showDate)

            ).collect(Collectors.toList());
        }else{
            return Collections.emptyList();
        }
    }

    public List<Show> getShowsByScreenId(Long screenId, String showDate) {
        Optional<Screen> optionalScreen = screenRepository.findById(screenId);

        if(optionalScreen.isPresent()){
            Screen screen = optionalScreen.get();
            List<Show> shows = screen.getShows();
           return shows.stream().filter(
                   show -> show.getShowDate().equals(showDate)
           ).collect(Collectors.toList());

        }else{
            return Collections.emptyList();
        }
    }

    public Show getLastShowOfDay(Long screenId, String showDate) {
        Optional<Screen> optionalScreen = screenRepository.findById(screenId);

        if(optionalScreen.isPresent()){
            Screen screen = optionalScreen.get();
           List<Show> shows = screen.getShows();
         return  shows.stream().filter(show -> show.getShowDate().equals(showDate)).max(Comparator.comparing(Show::getEnd)).orElse(null);
        }else{
            return null;
        }
    }
}

