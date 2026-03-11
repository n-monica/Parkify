package com.parkify.backend.controller;

import com.parkify.backend.model.Parking;
import com.parkify.backend.model.Booking;
import com.parkify.backend.service.ParkingService;
import com.parkify.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/parkings")
@CrossOrigin(origins = "*")
public class ParkingController {

    @Autowired
    private ParkingService parkingService;

    @Autowired
    private BookingService bookingService;

    // GET all parkings OR filter by ownerId
    @GetMapping
    public List<Parking> getParkings(@RequestParam(required = false) Long ownerId) {
        if (ownerId != null) {
            return parkingService.getParkingsByOwner(ownerId);
        }
        return parkingService.getAllParkings();
    }

    // GET nearby parkings
    @GetMapping("/nearby")
    public List<Parking> getNearbyParkings(
        @RequestParam double lat,
        @RequestParam double lng,
        @RequestParam double radius) {
        return parkingService.getAllParkings();
    }

    // GET parking by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getParkingById(@PathVariable Long id) {
        Optional<Parking> parking = parkingService.getParkingById(id);
        if (parking.isPresent()) {
            return ResponseEntity.ok(parking.get());
        }
        return ResponseEntity.status(404).body("Parking not found");
    }

    // GET revenue for a parking
    @GetMapping("/{id}/revenue")
    public ResponseEntity<?> getParkingRevenue(@PathVariable Long id) {
        List<Booking> bookings = bookingService.getBookingsByParking(id);
        double totalRevenue = bookings.stream()
            .mapToDouble(b -> b.getAmount() != null ? b.getAmount() : 0)
            .sum();
        Map<String, Object> result = new HashMap<>();
        result.put("parkingId", id);
        result.put("totalRevenue", totalRevenue);
        result.put("totalBookings", bookings.size());
        return ResponseEntity.ok(result);
    }

    // POST add new parking
    @PostMapping
    public Parking addParking(@RequestBody Parking parking) {
        return parkingService.saveParking(parking);
    }

    // PUT update parking
    @PutMapping("/{id}")
    public ResponseEntity<?> updateParking(@PathVariable Long id, @RequestBody Parking updatedParking) {
        Optional<Parking> existing = parkingService.getParkingById(id);
        if (existing.isPresent()) {
            Parking p = existing.get();
            if (updatedParking.getName() != null) p.setName(updatedParking.getName());
            if (updatedParking.getStatus() != null) p.setStatus(updatedParking.getStatus());
            if (updatedParking.getHours() != null) p.setHours(updatedParking.getHours());
            if (updatedParking.getCarPrice() != null) p.setCarPrice(updatedParking.getCarPrice());
            if (updatedParking.getBikePrice() != null) p.setBikePrice(updatedParking.getBikePrice());
            if (updatedParking.getLat() != null) p.setLat(updatedParking.getLat());
            if (updatedParking.getLng() != null) p.setLng(updatedParking.getLng());
            return ResponseEntity.ok(parkingService.saveParking(p));
        }
        return ResponseEntity.status(404).body("Parking not found");
    }

    // DELETE parking
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParking(@PathVariable Long id) {
        parkingService.deleteParking(id);
        return ResponseEntity.ok("Parking deleted successfully");
    }
}