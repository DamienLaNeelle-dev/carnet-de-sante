package com.example.carnet_de_sante.domain.port;

import com.example.carnet_de_sante.domain.model.Medecin;
import com.example.carnet_de_sante.domain.model.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    
    Optional<Medecin> findByUserId(Long userId);
    
    Optional<Medecin> findByNumeroRpps(String numeroRpps);
    
    List<Medecin> findBySpecialite(Specialite specialite);
    
    List<Medecin> findByValideTrue();
    
    boolean existsByNumeroRpps(String numeroRpps);
}
