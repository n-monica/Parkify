package com.parkify.backend.controller;

import com.parkify.backend.model.Slot;
import com.parkify.backend.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@CrossOrigin(origins = "*")
public class SlotController {

    @Autowired
    private SlotService slotService;

    // GET all slots OR filter by parkingId
    @GetMapping
    public List<Slot> getSlots(@RequestParam(required = false) Long parkingId) {
        if (parkingId != null) {
            return slotService.getSlotsByParking(parkingId);
        }
        return slotService.getAllSlots();
    }

    // TEMPORARY - fix slot types based on slot number prefix
    // Visit: /api/slots/fix-types once, then remove this
    @GetMapping("/fix-types")
    public ResponseEntity<?> fixSlotTypes() {
        List<Slot> allSlots = slotService.getAllSlots();
        for (Slot s : allSlots) {
            if (s.getSlotNumber() != null) {
                if (s.getSlotNumber().toUpperCase().startsWith("B")) {
                    s.setType("bike");
                } else {
                    s.setType("car");
                }
                slotService.saveSlot(s);
            }
        }
        return ResponseEntity.ok("Fixed " + allSlots.size() + " slots!");
    }

    // GET slots by parking path variable
    @GetMapping("/parking/{parkingId}")
    public List<Slot> getSlotsByParking(@PathVariable Long parkingId) {
        return slotService.getSlotsByParking(parkingId);
    }

    // GET single slot by ID
    @GetMapping("/{id}")
    public Slot getSlotById(@PathVariable Long id) {
        return slotService.getSlotById(id).orElse(null);
    }

    // POST add new slot
    @PostMapping
    public Slot addSlot(@RequestBody Slot slot) {
        return slotService.saveSlot(slot);
    }

    // PUT update slot
    @PutMapping("/{id}")
    public Slot updateSlot(@PathVariable Long id, @RequestBody Slot updatedSlot) {
        updatedSlot.setId(id);
        return slotService.saveSlot(updatedSlot);
    }

    // DELETE slot
    @DeleteMapping("/{id}")
    public void deleteSlot(@PathVariable Long id) {
        slotService.deleteSlot(id);
    }
}
