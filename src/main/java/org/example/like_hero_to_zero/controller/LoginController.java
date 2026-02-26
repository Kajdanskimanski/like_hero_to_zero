package org.example.like_hero_to_zero.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

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