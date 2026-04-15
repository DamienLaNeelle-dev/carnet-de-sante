package com.example.carnet_de_sante.application.dto;

import com.example.carnet_de_sante.domain.model.Genre;
import com.example.carnet_de_sante.domain.model.GroupeSanguin;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterPatientRequest {
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotNull(message = "Le genre est obligatoire")
    private Genre genre;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;
    
    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;
    
    @NotBlank(message = "Le numéro de sécurité sociale est obligatoire")
    @Pattern(regexp = "^[0-9]{15}$", message = "Le numéro de sécurité sociale doit contenir 15 chiffres")
    private String numeroSecu;
    
    private GroupeSanguin groupeSanguin;
    
    private String contactUrgence;
}
