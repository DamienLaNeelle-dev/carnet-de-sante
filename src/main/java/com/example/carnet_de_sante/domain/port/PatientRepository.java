package com.example.carnet_de_sante.domain.port;

import com.example.carnet_de_sante.domain.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    
    Optional<Patient> findByUserId(Long userId);
    
    Optional<Patient> findByNumeroSecu(String numeroSecu);
    
    boolean existsByNumeroSecu(String numeroSecu);
}
