package com.parkify.backend.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "parkings")
public class Parking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ownerId;
    private String name;
    private String location;
    private String city;        // ← add this
    private String status;
    private String hours;
    private Double carPrice;
    private Double bikePrice;
    private Double lat;
    private Double lng;
}