package com.parkify.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long parkingId;
    private Long slotId;
    private Long ownerId;
    private String customerName;
    private String vehicleNumber;
    private String type;
    private String duration;        // booked duration
    private String actualDuration;  // actual time spent
    private Double amount;
    private Double actualAmount;    // amount based on actual time
    private String status;          // confirmed, checked-in, completed, cancelled
    private LocalDate bookingDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
}