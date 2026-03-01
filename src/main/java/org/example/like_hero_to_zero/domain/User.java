package org.example.like_hero_to_zero.domain;

import jakarta.persistence.*;

/**
 * JPA-Entity für einen Anwendungsnutzer.
 * Wird in der Tabelle "users" gespeichert.
 *
 * Mögliche Rollen: "ROLE_SCIENTIST" (Standardnutzer) und "ROLE_ADMIN".
 * Das Passwort wird verschlüsselt gespeichert (siehe UserService).
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role = "ROLE_SCIENTIST"; // Standardrolle für neu registrierte Nutzer

    public User() {
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}