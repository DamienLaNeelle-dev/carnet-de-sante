package com.example.carnet_de_sante.infrastructure.driven.jpa;

import com.example.carnet_de_sante.domain.model.Specialite;
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
public class MedecinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "numero_rpps", unique = true, nullable = false, length = 11)
    private String numeroRPPS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private Specialite specialite;

    @Column(length = 255)
    private String etablissement;

    @Column(nullable = false)
    private boolean valide = false;

    @Column(name = "created_at")
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