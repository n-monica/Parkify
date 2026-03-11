package com.parkify.backend.repository;

import com.parkify.backend.model.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByParkingId(Long parkingId);
    List<Slot> findByStatus(String status);
}