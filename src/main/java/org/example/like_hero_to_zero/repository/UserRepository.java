package org.example.like_hero_to_zero.repository;

import org.example.like_hero_to_zero.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Wird von Spring Security verwendet
    Optional<User> findByUsername(String username);
}

