package com.example.carnet_de_sante.application.dto;

import com.example.carnet_de_sante.domain.model.Specialites;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterMedecinRequest {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 25)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 25)
    private String prenom;

    @NotNull(message = "La spécialité est obligatoire")
    private Specialites specialite;

    @NotBlank(message = "Le numéro RPPS est obligatoire")
    @Pattern(regexp = "^[0-9]{11}$", message = "Le numéro RPPS est obligatoire et doit contenir 11 chiffres")
    private String numeroRpps;
}
