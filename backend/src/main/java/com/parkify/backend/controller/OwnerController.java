
package com.parkify.backend.controller;

import com.parkify.backend.model.Owner;
import com.parkify.backend.service.OwnerService;
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

    // GET all owners
    @GetMapping
    public List<Owner> getAllOwners() {
        return ownerService.getAllOwners();
    }

    // GET owner by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOwnerById(@PathVariable Long id) {
        Optional<Owner> owner = ownerService.getOwnerById(id);
        if (owner.isPresent()) {
            return ResponseEntity.ok(owner.get());
        }
        return ResponseEntity.status(404).body("Owner not found");
    }

    // POST register new owner
    @PostMapping("/register")
    public ResponseEntity<?> registerOwner(@RequestBody Owner owner) {
        Optional<Owner> existing = ownerService.findByEmail(owner.getEmail());
        if (existing.isPresent()) {
            return ResponseEntity.status(409).body("Email already registered!");
        }
        owner.setStatus("active");
        Owner saved = ownerService.saveOwner(owner);
        return ResponseEntity.ok(saved);
    }

    // POST login using email
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Owner loginRequest) {
        Optional<Owner> owner = ownerService.findByEmail(loginRequest.getEmail());
        if (owner.isPresent() && owner.get().getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.ok(owner.get());
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    // PUT update owner
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOwner(@PathVariable Long id, @RequestBody Owner updatedOwner) {
        Optional<Owner> existing = ownerService.getOwnerById(id);
        if (existing.isPresent()) {
            updatedOwner.setId(id);
            return ResponseEntity.ok(ownerService.saveOwner(updatedOwner));
        }
        return ResponseEntity.status(404).body("Owner not found");
    }

    // DELETE owner
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOwner(@PathVariable Long id) {
        ownerService.deleteOwner(id);
        return ResponseEntity.ok("Owner deleted successfully");
    }
}