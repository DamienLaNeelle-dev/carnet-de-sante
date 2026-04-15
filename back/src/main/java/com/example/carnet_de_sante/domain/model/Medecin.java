package com.example.carnet_de_sante.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Entity
@Table(name = "medecins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name= "specialites", length = 100)
    private Specialites specialite;

    @Column(nullable = false, length = 11)
    private String numeroRpps;
}