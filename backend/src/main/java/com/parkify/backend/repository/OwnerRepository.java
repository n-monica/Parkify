package com.parkify.backend.repository;

import com.parkify.backend.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByUsername(String username);
    Optional<Owner> findByEmail(String email);
    List<Owner> findByStatus(String status);
}