package org.example.like_hero_to_zero.service;

import org.example.like_hero_to_zero.domain.User;
import org.example.like_hero_to_zero.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Benutzer finden (für Login)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Benutzer anlegen (Passwort wird verschlüsselt)
    public User createUser(String username, String rawPassword, String role) {
        System.out.println("CREATE USER WIRD AUFGERUFEN");
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(username, encodedPassword, role);
        return userRepository.save(user);
    }
}
