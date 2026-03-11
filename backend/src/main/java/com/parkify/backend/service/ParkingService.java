package com.parkify.backend.service;

import com.parkify.backend.model.Parking;
import com.parkify.backend.repository.ParkingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingService {

    @Autowired
    private ParkingRepository parkingRepository;

    public List<Parking> getAllParkings() {
        return parkingRepository.findAll();
    }

    public List<Parking> getParkingsByOwner(Long ownerId) {
        return parkingRepository.findByOwnerId(ownerId);
    }

    public Optional<Parking> getParkingById(Long id) {
        return parkingRepository.findById(id);
    }

    public Parking saveParking(Parking parking) {
        return parkingRepository.save(parking);
    }

    public void deleteParking(Long id) {
        parkingRepository.deleteById(id);
    }
}