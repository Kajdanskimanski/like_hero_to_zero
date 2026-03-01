package org.example.like_hero_to_zero.service;

import org.example.like_hero_to_zero.domain.User;
import org.example.like_hero_to_zero.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementierung von UserDetailsService für Spring Security.
 * Wird automatisch beim Login aufgerufen, um den Nutzer aus der Datenbank
 * zu laden und seine Rolle als GrantedAuthority zu übergeben.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Lädt einen Nutzer anhand seines Benutzernamens.
     * Konvertiert die eigene User-Entity in ein Spring-Security-UserDetails-Objekt,
     * das Benutzername, Passwort und Rolle enthält.
     *
     * @throws UsernameNotFoundException wenn kein Nutzer mit diesem Namen existiert
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));

        // Eigene User-Entity → Spring-Security-UserDetails-Objekt
        // Die Rolle (z.B. "ROLE_ADMIN") wird als einzige GrantedAuthority übergeben
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}