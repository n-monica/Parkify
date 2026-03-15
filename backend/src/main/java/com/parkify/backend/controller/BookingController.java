package com.parkify.backend.controller;

import com.parkify.backend.model.Booking;
import com.parkify.backend.model.Parking;
import com.parkify.backend.model.Slot;
import com.parkify.backend.service.BookingService;
import com.parkify.backend.service.SlotService;
import com.parkify.backend.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SlotService slotService;

    @Autowired
    private ParkingService parkingService;

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

    // POST new booking — marks slot as booked
    @PostMapping
    public ResponseEntity<?> addBooking(@RequestBody Booking booking) {
        List<Slot> slots = slotService.getSlotsByParking(booking.getParkingId());
        Optional<Slot> availableSlot = slots.stream()
            .filter(s -> (s.getStatus() == null || s.getStatus().equalsIgnoreCase("available"))
                && (booking.getType() == null || booking.getType().equalsIgnoreCase(s.getType())))
            .findFirst();

        if (availableSlot.isEmpty()) {
            return ResponseEntity.status(400).body("No available slots for this vehicle type!");
        }

        Slot slot = availableSlot.get();
        slot.setStatus("booked");
        slotService.saveSlot(slot);

        booking.setSlotId(slot.getId());
        booking.setStatus("confirmed");
        Booking saved = bookingService.saveBooking(booking);
        return ResponseEntity.ok(saved);
    }

    // POST check-in
    @PostMapping("/{id}/checkin")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        Optional<Booking> opt = bookingService.getBookingById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Booking not found");

        Booking booking = opt.get();
        booking.setCheckInTime(LocalDateTime.now());
        booking.setStatus("checked-in");
        bookingService.saveBooking(booking);
        return ResponseEntity.ok(booking);
    }

    // POST check-out
    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable Long id) {
        Optional<Booking> opt = bookingService.getBookingById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Booking not found");

        Booking booking = opt.get();
        LocalDateTime checkOut = LocalDateTime.now();
        booking.setCheckOutTime(checkOut);
        booking.setStatus("completed");

        if (booking.getCheckInTime() != null) {
            Duration actual = Duration.between(booking.getCheckInTime(), checkOut);
            long mins = actual.toMinutes();
            long hrs  = actual.toHours();
            booking.setActualDuration(hrs > 0 ? hrs + " hrs " + (mins % 60) + " mins" : mins + " mins");

            long billableHours = (long) Math.ceil(mins / 60.0);
            if (billableHours < 1) billableHours = 1;

            if (booking.getSlotId() != null) {
                Optional<Slot> slotOpt = slotService.getSlotById(booking.getSlotId());
                if (slotOpt.isPresent()) {
                    Slot slot = slotOpt.get();
                    slot.setStatus("available");
                    slotService.saveSlot(slot);

                    Optional<Parking> parkingOpt = parkingService.getParkingById(slot.getParkingId());
if (parkingOpt.isPresent()) {
    Parking parking = parkingOpt.get();
    double pricePerHour = "bike".equalsIgnoreCase(slot.getType())
        ? (parking.getBikePrice() != null ? parking.getBikePrice() : 0)
        : (parking.getCarPrice()  != null ? parking.getCarPrice()  : 0);
    booking.setActualAmount(billableHours * pricePerHour);
}
                }
            }
        } else {
            if (booking.getSlotId() != null) {
                slotService.getSlotById(booking.getSlotId()).ifPresent(slot -> {
                    slot.setStatus("available");
                    slotService.saveSlot(slot);
                });
            }
        }

        bookingService.saveBooking(booking);
        return ResponseEntity.ok(booking);
    }

    // DELETE booking
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }
}