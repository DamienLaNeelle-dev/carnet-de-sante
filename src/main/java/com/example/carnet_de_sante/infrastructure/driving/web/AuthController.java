package com.example.carnet_de_sante.infrastructure.driving.web;

import com.example.carnet_de_sante.application.dto.RegisterMedecinRequest;
import com.example.carnet_de_sante.application.dto.RegisterPatientRequest;
import com.example.carnet_de_sante.application.service.UserService;
import com.example.carnet_de_sante.domain.model.Medecin;
import com.example.carnet_de_sante.domain.model.Patient;
import com.example.carnet_de_sante.domain.model.User;
import com.example.carnet_de_sante.domain.port.MedecinRepository;
import com.example.carnet_de_sante.domain.port.PatientRepository;
import com.example.carnet_de_sante.domain.port.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MedecinRepository medecinRepository;
    private final PatientRepository patientRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/register/patient")
    public ResponseEntity<Patient> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        try {
            Patient patient = userService.creerPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(patient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/register/patients")
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @PostMapping("/register/medecin")
    public ResponseEntity<Medecin> registerMedecin(@Valid @RequestBody RegisterMedecinRequest request) {
        try {
            Medecin medecin = userService.creerMedecin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(medecin);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/register/medecins")
    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API Carnet de Santé - Opérationnelle ✅");
    }
}