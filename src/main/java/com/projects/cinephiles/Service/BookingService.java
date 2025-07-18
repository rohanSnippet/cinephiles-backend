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
            //List<LockedSeats> existingLockedSeats = lockedSeatsRepo.findActiveLockedSeatsForShow(show, LocalDateTime.now());
            // Get a list of seat IDs that are already locked and not expired
            Set<String> alreadyLockedSeats = existingLockedSeats.stream()
                    .flatMap(lockedSeats -> lockedSeats.getSeatsId().stream())
                    .collect(Collectors.toSet());

            // Check if any requested seats are already locked
            for (String seatId : lockedSeatsRequest.getSeatsId()) {
                if (alreadyLockedSeats.contains(seatId)) {
                    System.out.println(seatId+" is already locked");
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
            System.out.println("All seats locked successfully.");
            return true; // Locking was successful
        } else {
            return false; // Show not found
        }
    }

    @Transactional
    //@Scheduled(fixedRate = 60000) // Runs every 60 seconds
    @Scheduled(cron = "0 */1 * * * *")
    public void releaseExpiredSeats() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        List<Show> shows = showRepo.findActiveShows(); // Ensure it fetches shows with active sessions
        //System.out.println("Active shows:"+shows);
        for (Show show : shows) {
            // Find the expired locked seats
            List<LockedSeats> expiredSeats = show.getLockedSeats().stream()
                    .filter(seat -> seat.getExpirationTime().isBefore(now))
                    .collect(Collectors.toList());

            // Log the expired seats for debugging purposes
          //  System.out.println("Expired seats: " + expiredSeats);

            // Remove expired seats from the show
            show.getLockedSeats().removeAll(expiredSeats);
            System.out.println("Unlocked all expired lockedSeats");
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
            System.out.println("LockedSeats"+lockedSeats);
            LocalDateTime expirationTime = lockedSeats.getExpirationTime();


            long remainingTime = expirationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    - now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            // Return remaining time in seconds, ensuring it's not negative
            return Math.max(remainingTime / 1000, 0);
        }


        return 0;
    }


    @Transactional
    public ResponseEntity<String> unlockSeats(Long showId, String username) {
        Optional<LockedSeats> optionalLockedSeats = lockedSeatsRepo.findByShowIdAndUser (showId, username);

        // If locked seats do not exist, return a not found response
        if (!optionalLockedSeats.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No locked seats found for this user.");
        }

        LockedSeats lockedSeats = optionalLockedSeats.get();
        System.out.println("The locked seats are :"+lockedSeats);
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
    public ResponseEntity<String> bookSeats(LockedSeatsRequests lockedSeatsRequests) {
        // Fetch the show details
        System.out.println("book seats called ... ");
        Optional<Show> optionalShow = showRepo.findById(lockedSeatsRequests.getShowId());
        if (!optionalShow.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No shows found.");
        }

        Show show = optionalShow.get();
        Screen screen = show.getScreen();

        // Fetch the locked seats for the user
        Optional<LockedSeats> optionalLockedSeats = lockedSeatsRepo.findByShowIdAndUser(
                lockedSeatsRequests.getShowId(), lockedSeatsRequests.getUser()
        );

        if (!optionalLockedSeats.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No locked seats found for the user.");
        }

        LockedSeats lockedSeats = optionalLockedSeats.get();

        // Validate that the locked seats are still valid
        LocalDateTime now = LocalDateTime.now();
        if (lockedSeats.getExpirationTime().isBefore(now)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The locked seats have expired.");
        }
        // Create a new booking
        Booking newBooking = new Booking();
        newBooking.setTheatreId(show.getTId());
        newBooking.setShow(show);
        newBooking.setSeatsIds(String.join(",", new ArrayList<>(lockedSeats.getSeatsId())));
        newBooking.setUser(lockedSeats.getUser());

        // Fetch the theatre and owner details
        Optional<Theatre> optheatre = theatreRepo.findById(show.getTId());
        if (!optheatre.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Theatre not found.");
        }

        Theatre theatre = optheatre.get();
        Owner owner = theatre.getOwner();
        newBooking.setOwner(owner.getId());
        bookingRepo.save(newBooking);

        // Create a new order
        Order order = new Order();
        order.setSeats(String.join(",", new ArrayList<>(lockedSeats.getSeatsId())));
        order.setBookingId(newBooking.getId());
        order.setBookingTime(LocalTime.now());
        order.setBookingDate(LocalDate.now());
        order.setShowId(show.getId());
        order.setScreenName(screen.getSname());
        order.setMovie(show.getMovie().getTitle());// Use DTO instead of entity
        order.setStatus("Booked");
        order.setTheatre(theatre.getId());
        order.setPoster(show.getMovie().getPoster());
        System.out.println("The screen name is :"+screen.getSname());
        User opUser = userRepo.getUserByUsername(lockedSeats.getUser());
        if (opUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        order.setUser(opUser);
        order.setUsername(lockedSeats.getUser());
        order.setTotalAmount(lockedSeats.getPrice() * lockedSeats.getSeatsId().size());
        orderRepo.save(order);

        // Update the show with the newly booked seats
        synchronized (this) { // Ensure thread safety for concurrent bookings
            show.getBooked().addAll(lockedSeats.getSeatsId());
            showRepo.save(show);
        }

        // Remove the locked seats after booking
        lockedSeatsRepo.delete(lockedSeats);

        return ResponseEntity.ok("Seats booked successfully.");
    }

}