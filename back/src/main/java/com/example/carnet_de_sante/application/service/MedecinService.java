package com.example.carnet_de_sante.application.service;

import com.example.carnet_de_sante.application.dto.RegisterMedecinRequest;
import com.example.carnet_de_sante.domain.model.Medecin;
import com.example.carnet_de_sante.domain.port.MedecinRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedecinService {

    private final MedecinRepository medecinRepository;

    @Transactional
    public Medecin creerMedecin(RegisterMedecinRequest request) {
        if (medecinRepository.existsByNumeroRpps(request.getNumeroRpps())) {
            throw new IllegalArgumentException("Un médecin avec ce numéro RPPS existe déjà");
        }

        Medecin medecin = Medecin.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .specialite(request.getSpecialite())
                .numeroRpps(request.getNumeroRpps())
                .build();

        return medecinRepository.save(medecin);
    }

    public Optional<Medecin> getMedecinById(Long id) {
        return medecinRepository.findById(id);
    }

    public Optional<Medecin> getMedecinByPatientId(Long patientId) {
        return medecinRepository.findMedecinByPatientId(patientId);
    }

    public List<Medecin> getAllMedecins() {
        return medecinRepository.findAll();
    }

    @Transactional
    public List<Medecin> creerMedecins(List<RegisterMedecinRequest> requests) {
        return requests.stream()
                .map(this::creerMedecin)
                .toList();
    }
}
