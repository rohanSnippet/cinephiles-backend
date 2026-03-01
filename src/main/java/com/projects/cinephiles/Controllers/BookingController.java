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
  // In BookingController.java
  @PostMapping("/lock-seats")
  public ResponseEntity<?> lockSeats(@RequestBody LockedSeatsRequests lockedSeatsRequest) {
    Long expiresAtMs = bookingService.lockSeats(lockedSeatsRequest);

    if (expiresAtMs != null) {
      // Return the exact unix timestamp deadline
      return ResponseEntity.ok(expiresAtMs);
    } else {
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Seats are already locked by someone else.");
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

  @DeleteMapping("/cancel-seats")
  public ResponseEntity<String> cancelSeats(@RequestParam Long showId, @RequestParam String user){
    System.out.println("cancel seats called....");
    return bookingService.cancelSeats(showId, user);
  }

//  // 5. Unlock Seats on Cancellation
//  @DeleteMapping("/unlock-seats")
//  public ResponseEntity<String> unlockSeats(@RequestParam Long showId,@RequestParam String user) {
//    System.out.println("unlock seats is called.....");
//    return bookingService.unlockSeats(showId, user);
//  }

  @GetMapping("/unlock")
  public boolean unlock(){
    return true;
  }

}

