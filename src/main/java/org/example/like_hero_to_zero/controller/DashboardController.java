package org.example.like_hero_to_zero.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller für die Dashboard-Einstiegsseite.
 * Leitet Nutzer je nach Rolle auf die passende Dashboard-Ansicht weiter.
 */
@Controller
public class DashboardController {

    /**
     * Zeigt das Dashboard an.
     * Admins werden auf ein separates Admin-Dashboard weitergeleitet,
     * alle anderen Nutzer sehen das Standard-Dashboard.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());

        // Rollenprüfung: Admins erhalten eine erweiterte Ansicht mit Verwaltungsfunktionen
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "admin-dashboard";
        }

        return "dashboard";
    }
}