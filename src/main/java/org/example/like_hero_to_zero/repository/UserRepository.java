package org.example.like_hero_to_zero.repository;

import org.example.like_hero_to_zero.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository für den Datenbankzugriff auf Nutzer.
 * Spring Data JPA generiert die Implementierung automatisch zur Laufzeit.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Wird für Login und Registrierung genutzt (Eindeutigkeitsprüfung & Authentifizierung)
    Optional<User> findByUsername(String username);
}