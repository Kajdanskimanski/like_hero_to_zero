package org.example.like_hero_to_zero.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller für die Login-Seite.
 * Die eigentliche Authentifizierung übernimmt Spring Security –
 * dieser Controller ist nur für die Anzeige von Status- und Fehlermeldungen zuständig.
 */
@Controller
public class LoginController {

    /**
     * Zeigt das Login-Formular an.
     * Spring Security hängt bei fehlgeschlagener Anmeldung automatisch "?error"
     * und bei erfolgreichem Logout "?logout" an die URL – diese werden hier ausgewertet.
     */
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                Model model) {

        if (error != null) {
            model.addAttribute("error", "Benutzername oder Passwort ist falsch!");
        }

        if (logout != null) {
            model.addAttribute("message", "Sie wurden erfolgreich abgemeldet.");
        }

        return "login";
    }
}