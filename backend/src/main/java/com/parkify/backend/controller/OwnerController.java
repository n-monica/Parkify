package com.parkify.backend.controller;

import com.parkify.backend.model.Owner;
import com.parkify.backend.model.Parking;
import com.parkify.backend.service.OwnerService;
import com.parkify.backend.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/owners")
@CrossOrigin(origins = "*")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    @Autowired
    private ParkingService parkingService;

    @GetMapping
    public List<Owner> getAllOwners() { return ownerService.getAllOwners(); }

    @GetMapping("/pending")
    public List<Owner> getPendingOwners() { return ownerService.getOwnersByStatus("pending"); }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOwnerById(@PathVariable Long id) {
        return ownerService.getOwnerById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(404).build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerOwner(@RequestBody Owner owner) {
        if (ownerService.findByEmail(owner.getEmail()).isPresent())
            return ResponseEntity.status(409).body("Email already registered!");
        owner.setStatus("pending");
        ownerService.saveOwner(owner);
        return ResponseEntity.ok("Registration submitted. Awaiting admin approval.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Owner loginRequest) {
        Optional<Owner> owner = ownerService.findByEmail(loginRequest.getEmail());
        if (owner.isEmpty() || !owner.get().getPassword().equals(loginRequest.getPassword()))
            return ResponseEntity.status(401).body("Invalid email or password.");
        if ("pending".equals(owner.get().getStatus()))
            return ResponseEntity.status(403).body("PENDING");
        if ("rejected".equals(owner.get().getStatus()))
            return ResponseEntity.status(403).body("REJECTED");
        return ResponseEntity.ok(owner.get());
    }

    // KEY CHANGE: approve now auto-creates a Parking row from owner's registration details
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveOwner(@PathVariable Long id) {
        return ownerService.getOwnerById(id).map(o -> {
            o.setStatus("active");
            ownerService.saveOwner(o);

            // Only create parking if one doesn't already exist for this owner
            List<Parking> existing = parkingService.getParkingsByOwner(o.getId());
            if (existing.isEmpty()) {
                Parking parking = new Parking();
                parking.setOwnerId(o.getId());
                parking.setName(o.getParkingName());
                parking.setLocation(o.getParkingAddress() + ", " + o.getCity());
                parking.setStatus("Open");
                parking.setCarPrice(0.0);
                parking.setBikePrice(0.0);
                parkingService.saveParking(parking);
            }

            return ResponseEntity.ok("Owner approved and parking created.");
        }).orElse(ResponseEntity.status(404).build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectOwner(@PathVariable Long id) {
        return ownerService.getOwnerById(id).map(o -> {
            o.setStatus("rejected");
            ownerService.saveOwner(o);
            return ResponseEntity.ok("Owner rejected.");
        }).orElse(ResponseEntity.status(404).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOwner(@PathVariable Long id, @RequestBody Owner updatedOwner) {
        return ownerService.getOwnerById(id).map(existing -> {
            updatedOwner.setId(id);
            return ResponseEntity.ok(ownerService.saveOwner(updatedOwner));
        }).orElse(ResponseEntity.status(404).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.ok("Owner deleted.");
    }
}