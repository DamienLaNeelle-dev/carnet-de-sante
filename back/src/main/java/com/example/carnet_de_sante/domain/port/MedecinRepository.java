package com.example.carnet_de_sante.domain.port;

import com.example.carnet_de_sante.domain.model.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {

    boolean existsByNumeroRpps(String numeroRpps);

    Optional<Medecin> findByNumeroRpps(String numeroRpps);

    @Query("SELECT p.medecinTraitant FROM Patient p WHERE p.id = :patientId")
    Optional<Medecin> findMedecinByPatientId(@Param("patientId") Long patientId);
}
