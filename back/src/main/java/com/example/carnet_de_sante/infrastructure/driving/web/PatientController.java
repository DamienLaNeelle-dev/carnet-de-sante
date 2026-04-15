package com.example.carnet_de_sante.infrastructure.driving.web;

import com.example.carnet_de_sante.domain.model.Patient;
import com.example.carnet_de_sante.domain.port.PatientRepository;
import com.example.carnet_de_sante.domain.port.MedecinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;

    @GetMapping
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Patient> getPatientByUserId(@PathVariable Long userId) {
        return patientRepository.findByUser_Id(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/medecin-traitant")
    public ResponseEntity<Patient> assignMedecinTraitant(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        
        return patientRepository.findByUser_Id(userId)
                .flatMap(patient -> {
                    String numeroRPPS = request.get("numeroRPPS");
                    if (numeroRPPS == null || numeroRPPS.isEmpty()) {
                        return java.util.Optional.empty();
                    }
                    
                    return medecinRepository.findByNumeroRpps(numeroRPPS)
                            .map(medecin -> {
                                patient.setMedecinTraitant(medecin);
                                Patient updated = patientRepository.save(patient);
                                return ResponseEntity.ok(updated);
                            });
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
