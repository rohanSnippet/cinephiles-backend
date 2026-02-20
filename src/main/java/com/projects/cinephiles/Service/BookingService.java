package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.LockedSeatsRequests;
import com.projects.cinephiles.Repo.*;
import com.projects.cinephiles.models.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ShowRepo showRepo;
    @Autowired
    private LockedSeatsRepo lockedSeatsRepo;
    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private TheatreRepo theatreRepo;
    @Autowired
    private OrderRepo orderRepo;

    @Transactional
    public boolean lockSeats(LockedSeatsRequests lockedSeatsRequest) {
        Optional<Show> optionalShow = showRepo.findById(lockedSeatsRequest.getShowId());

        if (optionalShow.isPresent()) {
            Show show = optionalShow.get();

            // ONLY look for locks that are still active in the future
            List<LockedSeats> existingLockedSeats = lockedSeatsRepo.findByShowAndExpirationTimeAfter(show, LocalDateTime.now());
            
            Set<String> alreadyLockedSeats = existingLockedSeats.stream()
                    .flatMap(lockedSeats -> lockedSeats.getSeatsId().stream())
                    .collect(Collectors.toSet());

            for (String seatId : lockedSeatsRequest.getSeatsId()) {
                if (alreadyLockedSeats.contains(seatId)) {
                    System.out.println(seatId + " is already locked");
                    return false; 
                }
            }

            LockedSeats lockedSeats = new LockedSeats();
            lockedSeats.setShow(show);
            lockedSeats.setSeatsId(lockedSeatsRequest.getSeatsId());
            lockedSeats.setPrice(lockedSeatsRequest.getPrice());
            lockedSeats.setTierName(lockedSeatsRequest.getTierName());
            lockedSeats.setCgst(lockedSeatsRequest.getCgst());
            lockedSeats.setSgst(lockedSeatsRequest.getSgst());
            lockedSeats.setUserEmail(lockedSeatsRequest.getUser());
            // Set expiration to exactly 7 minutes from now
            lockedSeats.setExpirationTime(LocalDateTime.now().plusMinutes(7)); 

            lockedSeatsRepo.save(lockedSeats);
            return true; 
        } else {
            return false;
        }
    }

    // Active Cleanup: Removes dead rows from DB to save space.
    // Even if this delays, the query in lockSeats() ignores expired rows anyway.
    @Transactional
    @Scheduled(cron = "0 */1 * * * *")
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<Show> shows = showRepo.findActiveShows(); 
        
        for (Show show : shows) {
            List<LockedSeats> expiredSeats = show.getLockedSeats().stream()
                    .filter(seat -> seat.getExpirationTime().isBefore(now))
                    .collect(Collectors.toList());

            if (!expiredSeats.isEmpty()) {
                show.getLockedSeats().removeAll(expiredSeats);
                lockedSeatsRepo.deleteAll(expiredSeats);
                showRepo.save(show);
            }
        }
    }

    @Transactional
    public long getRemainingTime(Long showId, String userEmail) {
        LocalDateTime now = LocalDateTime.now();
        Optional<LockedSeats> optionalLockedSeats = lockedSeatsRepo.findByShowIdAndUserEmail(showId, userEmail);

        if (optionalLockedSeats.isPresent()) {
            LocalDateTime expirationTime = optionalLockedSeats.get().getExpirationTime();
            long remainingTime = expirationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    - now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return Math.max(remainingTime / 1000, 0);
        }
        return 0;
    }

    // Consolidated method for both explicit cancellation and early unlock
    @Transactional
    public ResponseEntity<String> cancelSeats(Long showId, String user) {
        Optional<LockedSeats> opt = lockedSeatsRepo.findByShowIdAndUserEmail(showId, user);
        if (!opt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No locked seats found.");
        }
        
        lockedSeatsRepo.delete(opt.get());
        return ResponseEntity.ok("Seats unlocked successfully.");
    }

    // Alias unlockSeats to cancelSeats to prevent confusion
    @Transactional
    public ResponseEntity<String> unlockSeats(Long showId, String username) {
        return cancelSeats(showId, username);
    }

    @Transactional
    public ResponseEntity<String> bookSeats(LockedSeatsRequests lockedSeatsRequests) {
        Optional<Show> optionalShow = showRepo.findById(lockedSeatsRequests.getShowId());
        if (!optionalShow.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No shows found.");

        Show show = optionalShow.get();
        Screen screen = show.getScreen();

        Optional<LockedSeats> optionalLockedSeats = lockedSeatsRepo.findByShowIdAndUserEmail(
                lockedSeatsRequests.getShowId(), lockedSeatsRequests.getUser()
        );

        if (!optionalLockedSeats.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No locked seats found for the user.");
        }

        LockedSeats lockedSeats = optionalLockedSeats.get();

        // Final security check: ensure it hasn't expired before processing payment
        if (lockedSeats.getExpirationTime().isBefore(LocalDateTime.now())) {
            lockedSeatsRepo.delete(lockedSeats); // clean up
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The locked seats have expired.");
        }

        Booking newBooking = new Booking();
        newBooking.setTheatreId(show.getTId());
        newBooking.setShow(show);
        newBooking.setBookingID(lockedSeatsRequests.getBookingID());
        newBooking.setSeatsIds(String.join(",", new ArrayList<>(lockedSeats.getSeatsId())));
        newBooking.setUserEmail(lockedSeats.getUserEmail());
        newBooking.setBaseAmount(lockedSeatsRequests.getPrice());
        newBooking.setTotalAmount(lockedSeatsRequests.getPrice()+lockedSeatsRequests.getCgst()+lockedSeatsRequests.getSgst());
        newBooking.setCgst(lockedSeatsRequests.getCgst());
        newBooking.setSgst(lockedSeatsRequests.getSgst());
        newBooking.setTierName(lockedSeatsRequests.getTierName());

        Optional<Theatre> optheatre = theatreRepo.findById(show.getTId());
        if (!optheatre.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Theatre not found.");

        Theatre theatre = optheatre.get();
        newBooking.setOwner(theatre.getOwner().getId());
        bookingRepo.save(newBooking);

        Order order = new Order();
        order.setSeats(String.join(",", new ArrayList<>(lockedSeats.getSeatsId())));
        order.setBookingId(newBooking.getId());
        order.setBookingTime(LocalTime.now());
        order.setBookingDate(LocalDate.now());
        order.setShowId(show.getId());
        order.setScreenName(screen.getSname());
        order.setMovie(show.getMovie().getTitle());
        order.setStatus("Booked");
        order.setTheatre(theatre.getId());
        order.setPoster(show.getMovie().getPoster());

        User opUser = userRepo.getUserByUsername(lockedSeats.getUserEmail());
        if (opUser == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");

        order.setUser(opUser);
        order.setUsername(lockedSeats.getUserEmail());
        order.setTotalAmount(lockedSeats.getPrice() * lockedSeats.getSeatsId().size());
        orderRepo.save(order);
        
        // Update the show's booked seats with the newly booked seats
        show.getBooked().addAll(lockedSeats.getSeatsId());
        showRepo.save(show);

        // Remove lock because it is now officially booked
        lockedSeatsRepo.delete(lockedSeats);

        return ResponseEntity.ok("Seats booked successfully.");
    }
}