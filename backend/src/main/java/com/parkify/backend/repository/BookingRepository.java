package com.parkify.backend.repository;

import com.parkify.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByOwnerId(Long ownerId);
    List<Booking> findByStatus(String status);
    List<Booking> findByParkingId(Long parkingId);
}