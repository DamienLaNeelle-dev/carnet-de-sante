package com.example.carnet_de_sante.domain.port;

import com.example.carnet_de_sante.domain.model.PatientFile;
import com.example.carnet_de_sante.domain.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientFileRepository extends JpaRepository<PatientFile, Long> {
    List<PatientFile> findByPatientIdAndDeletedAtIsNull(Long patientId);
    
    List<PatientFile> findByPatient(Patient patient);
}
