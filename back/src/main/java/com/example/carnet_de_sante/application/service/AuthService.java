package com.example.carnet_de_sante.application.service;

import com.example.carnet_de_sante.application.dto.LoginRequest;
import com.example.carnet_de_sante.application.dto.LoginResponse;
import com.example.carnet_de_sante.domain.model.User;
import com.example.carnet_de_sante.domain.port.UserRepository;
import com.example.carnet_de_sante.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Définir l'authentification dans le contexte
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer le token JWT
        String token = tokenProvider.generateToken(authentication);

        // Récupérer les infos de l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Construire la réponse
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .role(user.getRole())
                .userId(user.getId())
                .build();
    }
}
