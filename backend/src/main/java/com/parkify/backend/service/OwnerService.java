package com.parkify.backend.service;

import com.parkify.backend.model.Owner;
import com.parkify.backend.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;

    public List<Owner> getAllOwners() { return ownerRepository.findAll(); }
    public List<Owner> getOwnersByStatus(String status) { return ownerRepository.findByStatus(status); }
    public Optional<Owner> getOwnerById(Long id) { return ownerRepository.findById(id); }
    public Owner saveOwner(Owner owner) { return ownerRepository.save(owner); }
    public Optional<Owner> findByUsername(String username) { return ownerRepository.findByUsername(username); }
    public Optional<Owner> findByEmail(String email) { return ownerRepository.findByEmail(email); }
    public void deleteOwner(Long id) { ownerRepository.deleteById(id); }
}