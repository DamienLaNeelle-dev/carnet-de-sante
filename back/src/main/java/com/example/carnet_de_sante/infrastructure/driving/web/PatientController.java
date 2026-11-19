package com.example.carnet_de_sante.infrastructure.driving.web;

import com.example.carnet_de_sante.domain.model.Patient;
import com.example.carnet_de_sante.domain.port.PatientRepository;
import com.example.carnet_de_sante.domain.port.MedecinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<Patient> getPatientByUserId(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Patient authenticatedPatient = patientRepository
                .findByUser_Email(userDetails.getUsername())
                .orElse(null);

        if (authenticatedPatient == null || !authenticatedPatient.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(authenticatedPatient);
    }

    @PostMapping("/{userId}/medecin-traitant")
    public ResponseEntity<Patient> assignMedecinTraitant(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Patient authenticatedPatient = patientRepository
                .findByUser_Email(userDetails.getUsername())
                .orElse(null);

        if (authenticatedPatient == null || !authenticatedPatient.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String numeroRPPS = request.get("numeroRPPS");
        if (numeroRPPS == null || numeroRPPS.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return medecinRepository.findByNumeroRpps(numeroRPPS)
                .map(medecin -> {
                    authenticatedPatient.setMedecinTraitant(medecin);
                    return ResponseEntity.ok(patientRepository.save(authenticatedPatient));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
