package org.example.like_hero_to_zero.service;

import org.example.like_hero_to_zero.domain.User;
import org.example.like_hero_to_zero.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service für die Benutzerverwaltung.
 * Kapselt den Datenbankzugriff und stellt sicher, dass Passwörter
 * niemals im Klartext gespeichert werden.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Wird bei Login und Registrierung zur Eindeutigkeitsprüfung genutzt
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Legt einen neuen Benutzer an.
     * Das Passwort wird vor dem Speichern mit BCrypt verschlüsselt –
     * das Klartextpasswort wird nie persistiert.
     */
    public User createUser(String username, String rawPassword, String role) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword, role);
        return userRepository.save(user);
    }
}