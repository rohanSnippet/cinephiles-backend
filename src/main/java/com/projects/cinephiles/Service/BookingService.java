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
  private  OrderRepo orderRepo;

    @Transactional
    public boolean lockSeats(LockedSeatsRequests lockedSeatsRequest) {
        // Fetch the Show object by showId
        Optional<Show> optionalShow = showRepo.findById(lockedSeatsRequest.getShowId());

        if (optionalShow.isPresent()) {
            Show show = optionalShow.get();

            // Check for already locked seats for the show
            List<LockedSeats> existingLockedSeats = lockedSeatsRepo.findByShowAndExpirationTimeAfter(show, LocalDateTime.now());

            // Get a list of seat IDs that are already locked and not expired
            Set<String> alreadyLockedSeats = existingLockedSeats.stream()
                    .flatMap(lockedSeats -> lockedSeats.getSeatsId().stream())
                    .collect(Collectors.toSet());

            // Check if any requested seats are already locked
            for (String seatId : lockedSeatsRequest.getSeatsId()) {
                if (alreadyLockedSeats.contains(seatId)) {
                    return false; // Return false if any seat is already locked
                }
            }

            // Proceed to lock the seats as none of them are locked
            LockedSeats lockedSeats = new LockedSeats();
            lockedSeats.setShow(show);
            lockedSeats.setSeatsId(lockedSeatsRequest.getSeatsId());
            lockedSeats.setPrice(lockedSeatsRequest.getPrice());
            lockedSeats.setUser(lockedSeatsRequest.getUser());
            lockedSeats.setExpirationTime(LocalDateTime.now().plusMinutes(2)); // Set expiration time if needed

            // Save the LockedSeats instance
            lockedSeatsRepo.save(lockedSeats);

            return true; // Locking was successful
        } else {
            return false; // Show not found
        }
    }



    @Transactional
    @Scheduled(fixedRate = 20000) // Runs every 20 seconds
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<Show> shows = showRepo.findAll(); // Ensure it fetches shows with active sessions

        for (Show show : shows) {
            // Find the expired locked seats
            List<LockedSeats> expiredSeats = show.getLockedSeats().stream()
                    .filter(seat -> seat.getExpirationTime().isBefore(now))
                    .collect(Collectors.toList());

            // Log the expired seats for debugging purposes
            System.out.println("Expired seats: " + expiredSeats);

            // Remove expired seats from the show
            show.getLockedSeats().removeAll(expiredSeats);

            // Delete expired seats from the database
            lockedSeatsRepo.deleteAll(expiredSeats);

            // Persist the updated show
            showRepo.save(show);
        }
    }



    @Transactional
    public long getRemainingTime(Long showId, String user) {
        LocalDateTime now = LocalDateTime.now();

        // Fetch the LockedSeats object for the given showId and user
        Optional<LockedSeats> optionalLockedSeats = lockedSeatsRepo.findByShowIdAndUser(showId, user);

        // If locked seats exist, calculate the remaining time
        if (optionalLockedSeats.isPresent()) {
            LockedSeats lockedSeats = optionalLockedSeats.get();
            LocalDateTime expirationTime = lockedSeats.getExpirationTime();


            long remainingTime = expirationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    - now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Return remaining time in seconds, ensuring it's not negative
            return Math.max(remainingTime / 1000, 0);
        }


        return 0;
    }


    @Transactional
    public ResponseEntity<String> unlockSeats(Long showId, String user) {
        Optional<LockedSeats> optionalLockedSeats = lockedSeatsRepo.findByShowIdAndUser (showId, user);

        // If locked seats do not exist, return a not found response
        if (!optionalLockedSeats.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No locked seats found for this user.");
        }

        LockedSeats lockedSeats = optionalLockedSeats.get();
        LocalDateTime expirationTime = lockedSeats.getExpirationTime();

        // Check if the expiration time has passed
        if (LocalDateTime.now().isAfter(expirationTime)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot unlock seats, the time has expired.");
        }

        // Delete the locked seats
        lockedSeatsRepo.delete(lockedSeats);
        return ResponseEntity.ok("Seats Unlocked");
    }

    @Transactional
    public  ResponseEntity<String>  bookSeats(LockedSeatsRequests lockedSeatsRequests) {
        Optional<Show> optionalShow = showRepo.findById(lockedSeatsRequests.getShowId());
        System.out.println("Entered in bookSeats");
        if (!optionalShow.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No shows found.");

        }else{
            Optional<LockedSeats> optionalLockedSeats = lockedSeatsRepo.findByShowIdAndUser (lockedSeatsRequests.getShowId(), lockedSeatsRequests.getUser());
            LockedSeats lockedSeats = optionalLockedSeats.get();
            Show show = optionalShow.get();
            Screen screen = show.getScreen();
            Booking newBooking = new Booking();
            newBooking.setTheatreId(show.getTId());
            newBooking.setShow(show);
            newBooking.setSeatsIds(String.join(",", new ArrayList<>(lockedSeats.getSeatsId())));
            newBooking.setUser(lockedSeats.getUser());//
            Optional<Theatre> optheatre = theatreRepo.findById(show.getTId());
            Theatre theatre = optheatre.get();
            Owner owner = theatre.getOwner();
            newBooking.setOwner(owner.getId());
            bookingRepo.save(newBooking);

            Order order = new Order();
            order.setSeats(String.join(",", new ArrayList<>(lockedSeats.getSeatsId())));
            order.setBookingTime(LocalTime.now());
            order.setBookingDate(LocalDate.now());
            order.setShow(show);
            order.setScreenName(screen.getSname());
            User opUser = userRepo.getUserByUsername(lockedSeats.getUser());
            order.setUser(opUser);
            order.setUsername(lockedSeats.getUser());
            order.setTotalAmount(lockedSeats.getPrice()*lockedSeats.getSeatsId().size());
            orderRepo.save(order);


            show.getBooked().addAll(lockedSeats.getSeatsId());
            System.out.println("booked");
            showRepo.save(show);
            return ResponseEntity.ok("Seats Booked");
        }

    }


}