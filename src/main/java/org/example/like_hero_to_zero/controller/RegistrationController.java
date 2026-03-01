package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller für die Benutzerregistrierung.
 * Neue Nutzer erhalten automatisch die Rolle "ROLE_SCIENTIST".
 */
@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    /** Zeigt das Registrierungsformular an. */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    /**
     * Verarbeitet die Registrierungsanfrage.
     * Validiert Eingaben serverseitig, bevor der Nutzer angelegt wird.
     * Bei Erfolg wird zur Login-Seite weitergeleitet.
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String passwordConfirm,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // Passwörter müssen übereinstimmen
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "Die Passwörter stimmen nicht überein!");
            return "register";
        }

        // Mindestlänge des Passworts prüfen
        if (password.length() < 4) {
            model.addAttribute("error", "Das Passwort muss mindestens 4 Zeichen lang sein!");
            return "register";
        }

        // Benutzername darf nicht leer sein
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Bitte geben Sie einen Benutzernamen ein!");
            return "register";
        }

        try {
            // Eindeutigkeit des Benutzernamens sicherstellen
            if (userService.findByUsername(username).isPresent()) {
                model.addAttribute("error", "Benutzername bereits vergeben!");
                return "register";
            }

            // Neuen Benutzer mit Standardrolle anlegen
            userService.createUser(username, password, "ROLE_SCIENTIST");

            redirectAttributes.addFlashAttribute("success",
                    "Registrierung erfolgreich! Bitte melden Sie sich an.");

            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "Registrierung fehlgeschlagen: " + e.getMessage());
            return "register";
        }
    }
}