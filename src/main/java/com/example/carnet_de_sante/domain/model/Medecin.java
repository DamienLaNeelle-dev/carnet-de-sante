package com.example.carnet_de_sante.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "numero_rpps", nullable = false, unique = true, length = 11)
    private String numeroRpps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private Specialite specialite;

    @Column(length = 255)
    private String etablissement;

    @Column(nullable = false)
    @Builder.Default
    private Boolean valide = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}