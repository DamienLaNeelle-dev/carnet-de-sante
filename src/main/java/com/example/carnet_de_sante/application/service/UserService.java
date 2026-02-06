package com.example.carnet_de_sante.application.service;

import com.example.carnet_de_sante.application.dto.RegisterMedecinRequest;
import com.example.carnet_de_sante.application.dto.RegisterPatientRequest;
import com.example.carnet_de_sante.domain.model.*;
import com.example.carnet_de_sante.domain.port.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Patient creerPatient(RegisterPatientRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà");
        }

        if (patientRepository.existsByNumeroSecu(request.getNumeroSecu())) {
            throw new IllegalArgumentException("Ce numéro de sécurité sociale est déjà enregistré");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PATIENT)
                .build();
        user = userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .dateNaissance(request.getDateNaissance())
                .numeroSecu(request.getNumeroSecu())
                .groupeSanguin(request.getGroupeSanguin())
                .contactUrgence(request.getContactUrgence())
                .build();
        
        return patientRepository.save(patient);
    }

    @Transactional
    public Medecin creerMedecin(RegisterMedecinRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà");
        }

        if (medecinRepository.existsByNumeroRpps(request.getNumeroRpps())) {
            throw new IllegalArgumentException("Ce numéro RPPS est déjà enregistré");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.MEDECIN)
                .build();
        user = userRepository.save(user);

        Medecin medecin = Medecin.builder()
                .user(user)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .numeroRpps(request.getNumeroRpps())
                .specialite(request.getSpecialite())
                .etablissement(request.getEtablissement())
                .valide(false) // Nécessite validation manuelle
                .build();
        
        return medecinRepository.save(medecin);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    }

    public Patient getPatientProfile(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profil patient non trouvé"));
    }

    public Medecin getMedecinProfile(Long userId) {
        return medecinRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profil médecin non trouvé"));
    }
}
