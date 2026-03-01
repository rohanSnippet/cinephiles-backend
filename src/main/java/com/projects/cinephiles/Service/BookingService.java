package com.projects.cinephiles.Service;

import com.projects.cinephiles.DTO.LockedSeatsRequests;
import com.projects.cinephiles.Repo.*;
import com.projects.cinephiles.models.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ShowRepo showRepo;
    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private TheatreRepo theatreRepo;
    @Autowired
    private OrderRepo orderRepo;

    // Inject StringRedisTemplate instead of LockedSeatsRepo
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long LOCK_TIME_MINUTES = 7;

    public BookingService() {
    }

    // Helper to generate Redis keys
    private String getSeatKey(Long showId, String seatId) {
        return "lock:seat:" + showId + ":" + seatId;
    }
    private String getUserKey(Long showId, String userEmail) {
        return "lock:user:" + showId + ":" + userEmail;
    }

    @Transactional
    public Long lockSeats(LockedSeatsRequests request) {
        Optional<Show> optionalShow = showRepo.findById(request.getShowId());
        if (!optionalShow.isPresent()) return null;

        String userKey = getUserKey(request.getShowId(), request.getUser());
        List<String> successfullyLockedSeats = new ArrayList<>();

        // 1. Try to lock every seat using SETNX
        for (String seatId : request.getSeatsId()) {
            String seatKey = getSeatKey(request.getShowId(), seatId);
            // setIfAbsent is Redis SETNX. Returns true ONLY if key didn't exist.
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(seatKey, request.getUser(), Duration.ofMinutes(LOCK_TIME_MINUTES));

            if (Boolean.TRUE.equals(acquired)) {
                successfullyLockedSeats.add(seatKey);
            } else {
                // RACE CONDITION CAUGHT: Someone else just took this seat!
                // Rollback: Unlock the seats this user managed to lock before failing
                if (!successfullyLockedSeats.isEmpty()) {
                    redisTemplate.delete(successfullyLockedSeats);
                }
                return null; // Lock failed
            }
        }

        // 2. Save User Metadata (to quickly check what this user locked later)
        String userMetadata = String.join(",", request.getSeatsId());
        redisTemplate.opsForValue().set(userKey, userMetadata, Duration.ofMinutes(LOCK_TIME_MINUTES));

        // 3. Return the exact absolute timestamp (in ms) when this lock expires
        return System.currentTimeMillis() + (LOCK_TIME_MINUTES * 60 * 1000);
    }

    public long getRemainingTime(Long showId, String userEmail) {
        String userKey = getUserKey(showId, userEmail);
        Long expireInSeconds = redisTemplate.getExpire(userKey);
        return (expireInSeconds != null && expireInSeconds > 0) ? expireInSeconds : 0;
    }

    public ResponseEntity<String> cancelSeats(Long showId, String userEmail) {
        String userKey = getUserKey(showId, userEmail);
        String lockedSeatsStr = redisTemplate.opsForValue().get(userKey);

        if (lockedSeatsStr != null) {
            // Delete individual seat locks
            String[] seats = lockedSeatsStr.split(",");
            List<String> keysToDelete = new ArrayList<>();
            for (String seat : seats) {
                keysToDelete.add(getSeatKey(showId, seat));
            }
            keysToDelete.add(userKey);
            redisTemplate.delete(keysToDelete);
        }
        return ResponseEntity.ok("Seats unlocked successfully.");
    }

    @Transactional
    public ResponseEntity<String> bookSeats(LockedSeatsRequests request) {
        String userKey = getUserKey(request.getShowId(), request.getUser());
        String lockedSeatsStr = redisTemplate.opsForValue().get(userKey);

        // 1. Check if lock still exists in Redis
        if (lockedSeatsStr == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The locked seats have expired.");
        }

        // Proceed with DB inserts since lock is valid
        Optional<Show> optionalShow = showRepo.findById(request.getShowId());
        if (!optionalShow.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No shows found.");

        Show show = optionalShow.get();
        Screen screen = show.getScreen();
        Optional<Theatre> optheatre = theatreRepo.findById(show.getTId());
        if (!optheatre.isPresent()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Theatre not found.");

        // Create Booking
        Booking newBooking = new Booking();
        newBooking.setTheatreId(show.getTId());
        newBooking.setShow(show);
        newBooking.setBookingID(request.getBookingID());
        newBooking.setSeatsIds(lockedSeatsStr);
        newBooking.setUserEmail(request.getUser());
        newBooking.setBaseAmount(request.getPrice());
        newBooking.setTotalAmount(request.getPrice() + request.getCgst() + request.getSgst());
        newBooking.setCgst(request.getCgst());
        newBooking.setSgst(request.getSgst());
        newBooking.setTierName(request.getTierName());
        newBooking.setOwner(optheatre.get().getOwner().getId());
        bookingRepo.save(newBooking);

        // Create Order
        User opUser = userRepo.getUserByUsername(request.getUser());
        Order order = new Order();
        order.setSeats(lockedSeatsStr);
        order.setBookingId(newBooking.getId());
        order.setBookingTime(LocalTime.now());
        order.setBookingDate(LocalDate.now());
        order.setShowId(show.getId());
        order.setScreenName(screen.getSname());
        order.setMovie(show.getMovie().getTitle());
        order.setStatus("Booked");
        order.setTheatre(optheatre.get().getId());
        order.setPoster(show.getMovie().getPoster());
        order.setUser(opUser);
        order.setUsername(request.getUser());
        order.setTotalAmount(request.getPrice() * request.getSeatsId().size());
        orderRepo.save(order);

        // Update Show
        show.getBooked().addAll(request.getSeatsId());
        showRepo.save(show);

        // 2. Payment successful. Delete the Redis locks!
        cancelSeats(request.getShowId(), request.getUser());

        return ResponseEntity.ok("Seats booked successfully.");
    }
}