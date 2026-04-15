package com.example.carnet_de_sante.infrastructure.driving.web;

import com.example.carnet_de_sante.application.dto.RegisterMedecinRequest;
import com.example.carnet_de_sante.application.service.MedecinService;
import com.example.carnet_de_sante.domain.model.Medecin;
import com.example.carnet_de_sante.domain.port.MedecinRepository;
import com.example.carnet_de_sante.domain.port.PatientRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/medecins")
@RequiredArgsConstructor
public class MedecinController {

    private final MedecinService medecinService;
    private final PatientRepository patientRepository;

    @PostMapping
    public ResponseEntity<Medecin> addMedecin(@Valid @RequestBody RegisterMedecinRequest request) {
        try {
            Medecin medecin = medecinService.creerMedecin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(medecin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Medecin>> addMedecins(@Valid @RequestBody List<RegisterMedecinRequest> requests) {
        List<Medecin> medecins = medecinService.creerMedecins(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(medecins);
    }

    @GetMapping
    public ResponseEntity<List<Medecin>> getAllMedecins() {
        return ResponseEntity.ok(medecinService.getAllMedecins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medecin> getMedecinById(@PathVariable Long id) {
        return medecinService.getMedecinById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-patient/{userId}")
    public ResponseEntity<Medecin> getMedecinByPatientUserId(@PathVariable Long userId) {
        return patientRepository.findByUser_Id(userId)
                .flatMap(patient -> medecinService.getMedecinByPatientId(patient.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
