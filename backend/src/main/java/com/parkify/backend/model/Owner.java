package com.parkify.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "owners")
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String password;
    private String username;
    private String parkingName;
    private String parkingAddress;
    private String city;
    private String idProof;
    private Double lat;      // ← add this
    private Double lng;      // ← add this
    private String status;
}