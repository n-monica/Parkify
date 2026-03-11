package com.parkify.backend.controller;

import com.parkify.backend.model.Booking;
import com.parkify.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // GET all bookings
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // GET bookings by owner
    @GetMapping("/owner/{ownerId}")
    public List<Booking> getBookingsByOwner(@PathVariable Long ownerId) {
        return bookingService.getBookingsByOwner(ownerId);
    }

    // POST new booking
    @PostMapping
    public Booking addBooking(@RequestBody Booking booking) {
        return bookingService.saveBooking(booking);
    }

    // DELETE booking
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }
}
