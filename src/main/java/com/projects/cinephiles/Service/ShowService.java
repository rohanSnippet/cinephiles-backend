package com.projects.cinephiles.Service;

import com.projects.cinephiles.Enum.SeatStatus;
import com.projects.cinephiles.Enum.ShowStatus;
import com.projects.cinephiles.Repo.MovieRepo;
import com.projects.cinephiles.Repo.ScreenRepository;
import com.projects.cinephiles.Repo.ShowRepo;
import com.projects.cinephiles.Repo.TheatreRepo;
import com.projects.cinephiles.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
       List<Tier> tiers = screen.getTiers();

      int totalSeats = 0;
        for (Tier tier : tiers) {
            long availableSeatsInTier = tier.getSeats().stream()
                    .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                    .count();
            totalSeats += availableSeatsInTier;
        }
        System.out.println(totalSeats);


        show.setMovie(movie);
        show.setTheatre(theatre);
        show.setTId(theatre.getId());
        show.setMId(movie.getId());
        show.setSId(screen.getId());
        show.setRuntime(movie.getRuntime());
        show.setScreen(screen);
        show.setStatus(ShowStatus.AVAILABLE);
        show.setTitle(movie.getTitle());
        show.setBanner(movie.getBanner());
        show.setTotalSeats(totalSeats);
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


    @Transactional
    public List<Show> getShowsByMovieAndCity(Long movieId, List<String> city) {
        List<Show> shows = showRepo.findActiveShowsByMovieAndCity(movieId, city);
        shows.forEach(this::updateShowStatus);
        return shows;
    }

    private void updateShowStatus(Show show) {
        int totalSeats = show.getTotalSeats();
        int bookedSeats = (int) show.getBooked().stream()
                .filter(seat -> !seat.equalsIgnoreCase("NO_SEAT"))
                .count();
        System.out.println(bookedSeats);
        System.out.println(totalSeats);
        double occupancy = (double) bookedSeats / totalSeats;
        System.out.println(occupancy);//22
        if (occupancy == 1.0) {
            show.setStatus(ShowStatus.HOUSEFULL);

        } else if (occupancy >= 0.55) {
            show.setStatus(ShowStatus.FAST_FILLING);

        } else {
            show.setStatus(ShowStatus.AVAILABLE);

        }
        showRepo.save(show);
    }

    //Time comversion to be done "07:00"
    @Transactional
    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void deactivateExpiredShows() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("shows ");
        // Fetch shows where the start time has passed and are still active
        List<Show> expiredShows = showRepo.findAll().stream()
                .filter(show -> show.isActive() && LocalDateTime.parse(show.getStart()).isBefore(now))
                .toList();
        System.out.println(expiredShows);
        for (Show show : expiredShows) {
            show.setActive(false); // Deactivate the show
            showRepo.save(show); // Persist changes
        }
    }

    public void deleteShowById(Long sId) {
        if (!showRepo.existsById(sId)) {
            throw new IllegalArgumentException("Course with ID " + sId + " not found");
        }
        showRepo.deleteById(sId);

    }


}

