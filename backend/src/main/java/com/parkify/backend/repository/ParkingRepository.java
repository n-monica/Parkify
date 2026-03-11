package com.parkify.backend.repository;

import com.parkify.backend.model.Parking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParkingRepository extends JpaRepository<Parking, Long> {
    List<Parking> findByOwnerId(Long ownerId);
    List<Parking> findByStatus(String status);
}