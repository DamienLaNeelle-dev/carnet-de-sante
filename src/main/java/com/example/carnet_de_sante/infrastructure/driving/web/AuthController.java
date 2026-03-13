package com.example.carnet_de_sante.infrastructure.driving.web;

import com.example.carnet_de_sante.application.dto.LoginRequest;
import com.example.carnet_de_sante.application.dto.LoginResponse;
import com.example.carnet_de_sante.application.dto.RegisterPatientRequest;
import com.example.carnet_de_sante.application.service.AuthService;
import com.example.carnet_de_sante.application.service.UserService;
import com.example.carnet_de_sante.domain.model.Patient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * Endpoint de test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API Carnet de Santé - Opérationnelle ✅");
    }

    /**
     * Login - Authentification et génération du token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    /**
     * Inscription d'un nouveau patient
     */
    @PostMapping("/register/patient")
    public ResponseEntity<Patient> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        try {
            Patient patient = userService.creerPatient(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(patient);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

}
