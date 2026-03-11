package com.parkify.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

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
    private String type;
    private String duration;
    private Double amount;
    private String status;
    private LocalDate bookingDate;
}
