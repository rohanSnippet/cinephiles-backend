package com.projects.cinephiles.Controllers;

import com.projects.cinephiles.DTO.LockedSeatsRequests;
import com.projects.cinephiles.Repo.LockedSeatsRepo;
import com.projects.cinephiles.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

  @Autowired
    private LockedSeatsRepo lockedSeatsRepo;

  @Autowired
  private BookingService bookingService;

  // 1. Lock Seats
  @PostMapping("/lock-seats")
  public ResponseEntity<String> lockSeats(@RequestBody LockedSeatsRequests lockedSeatsRequest) {
    boolean isLocked = bookingService.lockSeats(lockedSeatsRequest);

    if (isLocked) {
      return ResponseEntity.ok("Seats locked successfully.");
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Show not found or seats already locked.");
    }
  }


  // 2. Get Remaining Time
  @GetMapping("/remaining-time")
  public long getRemainingTime(@RequestParam Long showId, @RequestParam String user) {
    return bookingService.getRemainingTime(showId, user);
  }

  // 4. Book Locked Seats
  @PostMapping("/book-seats")
  public ResponseEntity<String> bookSeats(@RequestBody LockedSeatsRequests lockedSeatsRequest ) {
    return bookingService.bookSeats(lockedSeatsRequest);
  }

  // 5. Unlock Seats on Cancellation
  @DeleteMapping("/unlock-seats")
  public ResponseEntity<String> unlockSeats(@RequestParam Long showId,@RequestParam String user) {
    System.out.println("unlock seats is called");
    return bookingService.unlockSeats(showId, user );
  }

}

