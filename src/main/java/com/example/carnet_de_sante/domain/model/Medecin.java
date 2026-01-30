package com.example.carnet_de_sante.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medecin {

    private Long id;
    private Long userId;
    private String nom;
    private String prenom;
    private String numeroRPPS;
    private Specialite specialite;
    private String etablissement;
    private boolean valide;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
