package org.example.like_hero_to_zero.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Konfiguration.
 * Definiert Zugriffsrechte, Login/Logout-Verhalten und Passwortverschlüsselung.
 *
 * Rollenkonzept:
 *   - Öffentlich:  /, /search, /register, /login, statische Ressourcen, /api/**
 *   - SCIENTIST:   /data/** (eigene Daten erfassen und verwalten)
 *   - ADMIN:       /admin/** (Einträge freigeben oder ablehnen) + alle SCIENTIST-Rechte
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * BCrypt als Passwort-Hashing-Algorithmus.
     * BCrypt ist bewusst langsam und Brute-Force-resistant –
     * daher der empfohlene Standard für Passwörter in Spring Security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Definiert die Sicherheitsregeln für alle HTTP-Anfragen.
     * Die Reihenfolge der requestMatchers ist relevant –
     * spezifischere Regeln müssen vor allgemeineren stehen.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Öffentlich zugängliche Seiten und Ressourcen
                        .requestMatchers("/", "/search", "/register", "/login", "/css/**", "/js/**", "/api/**").permitAll()
                        // Admin-Bereich: nur für Nutzer mit ROLE_ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Datenverwaltung: für eingeloggte Wissenschaftler und Admins
                        .requestMatchers("/data/**").hasAnyRole("SCIENTIST", "ADMIN")
                        // Alle anderen Seiten erfordern einen Login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        // Nach erfolgreichem Login immer zum Dashboard weiterleiten (true = immer, auch bei direktem Login-Aufruf)
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        // Nach Logout zurück zur Login-Seite mit Erfolgs-Parameter
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}