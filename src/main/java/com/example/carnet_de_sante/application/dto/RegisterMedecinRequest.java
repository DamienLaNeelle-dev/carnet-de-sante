package com.example.carnet_de_sante.application.dto;

import com.example.carnet_de_sante.domain.model.Specialite;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterMedecinRequest {
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;
    
    @NotBlank(message = "Le numéro RPPS est obligatoire")
    @Pattern(regexp = "^[0-9]{11}$", message = "Le numéro RPPS doit contenir 11 chiffres")
    private String numeroRpps;
    
    @NotNull(message = "La spécialité est obligatoire")
    private Specialite specialite;
    
    private String etablissement;
}
