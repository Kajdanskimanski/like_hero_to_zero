package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String passwordConfirm,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        // Validierung: Passwörter müssen übereinstimmen
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("error", "Die Passwörter stimmen nicht überein!");
            return "register";
        }

        // Validierung: Mindestlänge
        if (password.length() < 4) {
            model.addAttribute("error", "Das Passwort muss mindestens 4 Zeichen lang sein!");
            return "register";
        }

        // Validierung: Username darf nicht leer sein
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Bitte geben Sie einen Benutzernamen ein!");
            return "register";
        }

        try {  // ← HIER FEHLTE DAS try {
            // Prüfen ob Username bereits existiert
            if (userService.findByUsername(username).isPresent()) {
                model.addAttribute("error", "Benutzername bereits vergeben!");
                return "register";
            }

            System.out.println("🔍 DEBUG: Versuche User zu erstellen: " + username);


            // Neuen Benutzer erstellen
            userService.createUser(username, password, "ROLE_SCIENTIST");

            System.out.println("✅ DEBUG: User erfolgreich erstellt!");

            redirectAttributes.addFlashAttribute("success",
                    "Registrierung erfolgreich! Bitte melden Sie sich an.");

            return "redirect:/login";

        } catch (Exception e) {
            System.err.println("❌ DEBUG: Fehler bei Registrierung: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Registrierung fehlgeschlagen: " + e.getMessage());
            return "register";
        }
    }
}