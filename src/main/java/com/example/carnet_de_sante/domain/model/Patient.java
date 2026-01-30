package com.example.carnet_de_sante.domain.model;


import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String numeroSecu;
    private GroupeSanguin groupeSanguin;
    private String contactUrgence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getAge() {
        if (dateNaissance == null) {
            return 0;
        }
        return Period.between(dateNaissance, LocalDate.now()).getYears();
    }
}
