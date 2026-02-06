package com.example.carnet_de_sante.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register/**").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/test").permitAll()
                        .requestMatchers("/api/auth/users").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/api/patients/**").authenticated()

                        .requestMatchers("/api/medecins/**").authenticated()

                        .requestMatchers("/api/consultations/**").authenticated()

                        .requestMatchers("/api/ordonnances/**").authenticated()

                        .requestMatchers("/api/rendez-vous/**").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // API REST stateless
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}