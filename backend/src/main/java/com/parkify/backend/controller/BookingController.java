package com.parkify.backend.controller;

import com.parkify.backend.model.Booking;
import com.parkify.backend.model.Parking;
import com.parkify.backend.model.Slot;
import com.parkify.backend.service.BookingService;
import com.parkify.backend.service.SlotService;
import com.parkify.backend.service.ParkingService;
import com.parkify.backend.service.SmsService;
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

    @Autowired private BookingService bookingService;
    @Autowired private SlotService slotService;
    @Autowired private ParkingService parkingService;
    @Autowired private SmsService smsService;

    // GET all bookings
    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    // GET booking by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // GET bookings by owner
    @GetMapping("/owner/{ownerId}")
    public List<Booking> getBookingsByOwner(@PathVariable Long ownerId) {
        return bookingService.getBookingsByOwner(ownerId);
    }

    // POST new booking — assigns slot, generates OTP, saves ONCE
    @PostMapping
    public ResponseEntity<?> addBooking(@RequestBody Booking booking) {
        // Find an available slot matching vehicle type
        List<Slot> slots = slotService.getSlotsByParking(booking.getParkingId());
        Optional<Slot> availableSlot = slots.stream()
            .filter(s ->
                (s.getStatus() == null || s.getStatus().equalsIgnoreCase("available"))
                && (booking.getType() == null || booking.getType().equalsIgnoreCase(s.getType()))
            )
            .findFirst();

        if (availableSlot.isEmpty()) {
            return ResponseEntity.status(400).body("No available slots for this vehicle type!");
        }

        // Mark slot as booked
        Slot slot = availableSlot.get();
        slot.setStatus("booked");
        slotService.saveSlot(slot);

        // Attach slot and status to booking
        booking.setSlotId(slot.getId());
        booking.setStatus("confirmed");

        // Generate OTP and attach BEFORE saving — only one save call
        String otp = smsService.generateOtp();
        booking.setOtp(otp);

        System.out.println("=== BOOKING CREATE ===");
        System.out.println("OTP generated: " + otp);
        System.out.println("OTP on booking before save: " + booking.getOtp());

        // Single save — OTP is on the object going in
        Booking saved = bookingService.saveBooking(booking);

        System.out.println("OTP on saved object: " + saved.getOtp());
        System.out.println("Saved booking ID: " + saved.getId());
        System.out.println("======================");

        // Reload from DB to confirm OTP actually persisted
        bookingService.getBookingById(saved.getId()).ifPresent(reloaded ->
            System.out.println("OTP reloaded from DB: " + reloaded.getOtp())
        );

        // Send OTP via SMS
        String phone = booking.getUserPhone();
        if (phone != null && !phone.isBlank()) {
            smsService.sendOtp(phone, otp);
            System.out.println("OTP SMS sent to: " + phone);
        } else {
            System.out.println("⚠️ No phone number — OTP not sent via SMS");
        }

        return ResponseEntity.ok(saved);
    }

    // POST check-in — validates OTP, starts session
    @PostMapping("/{id}/checkin")
    public ResponseEntity<?> checkIn(@PathVariable Long id, @RequestParam String otp) {
        Optional<Booking> opt = bookingService.getBookingById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Booking not found");

        Booking booking = opt.get();

        System.out.println("=== CHECK-IN OTP DEBUG ===");
        System.out.println("Booking ID:   " + id);
        System.out.println("Entered OTP:  [" + otp + "]");
        System.out.println("Stored OTP:   [" + booking.getOtp() + "]");
        System.out.println("Match:        " + (booking.getOtp() != null && booking.getOtp().trim().equals(otp.trim())));
        System.out.println("==========================");

        if (booking.getOtp() == null || !booking.getOtp().trim().equals(otp.trim())) {
            return ResponseEntity.status(400).body("Invalid OTP!");
        }

        booking.setCheckInTime(LocalDateTime.now());
        booking.setStatus("checked-in");
        booking.setOtp(null); // OTP consumed — clear it
        Booking updated = bookingService.saveBooking(booking);

        return ResponseEntity.ok(updated);
    }

    // POST generate checkout OTP
    @PostMapping("/{id}/generate-checkout-otp")
    public ResponseEntity<?> generateCheckoutOtp(@PathVariable Long id) {
        Optional<Booking> opt = bookingService.getBookingById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Booking not found");

        Booking booking = opt.get();
        String otp = smsService.generateOtp();
        booking.setOtp(otp);
        bookingService.saveBooking(booking);

        if (booking.getUserPhone() != null && !booking.getUserPhone().isBlank()) {
            smsService.sendOtp(booking.getUserPhone(), otp);
        }

        System.out.println("Checkout OTP for booking #" + id + ": " + otp);
        // Return OTP in response so frontend can display it on screen
        return ResponseEntity.ok(java.util.Map.of("otp", otp, "message", "OTP generated!"));
    }

    // POST check-out — validates OTP, frees slot, calculates bill
    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable Long id, @RequestParam(required = false) String otp) {
        Optional<Booking> opt = bookingService.getBookingById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Booking not found");

        Booking booking = opt.get();

        // Validate OTP if provided
        if (otp != null && booking.getOtp() != null && !booking.getOtp().trim().equals(otp.trim())) {
            return ResponseEntity.status(400).body("Invalid OTP!");
        }

        LocalDateTime checkOut = LocalDateTime.now();
        booking.setCheckOutTime(checkOut);
        booking.setStatus("completed");
        booking.setOtp(null);

        // Calculate actual duration and bill
        if (booking.getCheckInTime() != null) {
            Duration actual = Duration.between(booking.getCheckInTime(), checkOut);
            long mins = actual.toMinutes();
            long hrs  = actual.toHours();
            booking.setActualDuration(hrs > 0
                ? hrs + " hrs " + (mins % 60) + " mins"
                : mins + " mins");

            final long billableHours = Math.max(1, (long) Math.ceil(mins / 60.0));

            if (booking.getSlotId() != null) {
                slotService.getSlotById(booking.getSlotId()).ifPresent(slot -> {
                    slot.setStatus("available");
                    slotService.saveSlot(slot);

                    parkingService.getParkingById(slot.getParkingId()).ifPresent(parking -> {
                        double rate = "bike".equalsIgnoreCase(slot.getType())
                            ? (parking.getBikePrice() != null ? parking.getBikePrice() : 0)
                            : (parking.getCarPrice()  != null ? parking.getCarPrice()  : 0);
                        booking.setActualAmount(billableHours * rate);
                    });
                });
            }
        } else {
            // No check-in time — just free the slot
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

    // GET bookings by parking
    @GetMapping("/parking/{parkingId}")
    public List<Booking> getBookingsByParking(@PathVariable Long parkingId) {
        return bookingService.getBookingsByParking(parkingId);
    }

    // POST cancel booking — frees slot, calculates refund
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        Optional<Booking> opt = bookingService.getBookingById(id);
        if (opt.isEmpty()) return ResponseEntity.status(404).body("Booking not found");

        Booking booking = opt.get();

        // Only allow cancel if not yet checked in
        if ("checked-in".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.status(400).body("Cannot cancel — you are already checked in!");
        }
        if ("completed".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.status(400).body("Cannot cancel a completed booking!");
        }
        if ("cancelled".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.status(400).body("Booking is already cancelled!");
        }

        // Calculate refund — 100% since no actual payment gateway
        double refundAmount = booking.getAmount() != null ? booking.getAmount() : 0;

        // Free the slot
        if (booking.getSlotId() != null) {
            slotService.getSlotById(booking.getSlotId()).ifPresent(slot -> {
                slot.setStatus("available");
                slotService.saveSlot(slot);
            });
        }

        // Update booking
        booking.setStatus("cancelled");
        booking.setOtp(null);
        booking.setActualAmount(0.0);
        bookingService.saveBooking(booking);

        System.out.println("Booking #" + id + " cancelled. Refund: ₹" + refundAmount);

        // Return refund info
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Booking cancelled successfully!",
            "refundAmount", refundAmount,
            "bookingId", id
        ));
    }

    // DELETE booking
    @DeleteMapping("/{id}")
    public void deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
    }
}