package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.service.EmissionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/data")
public class DataController {

    private final EmissionService emissionService;

    public DataController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    // Formular zum Hinzufügen
    @GetMapping("/add")
    public String showAddForm(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "data-add";
    }

    // Neue Daten speichern
    @PostMapping("/add")
    public String addData(@RequestParam String country,
                          @RequestParam Integer year,
                          @RequestParam Double co2Emissions,
                          @RequestParam(required = false) String source,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        try {
            Emission emission = new Emission();
            emission.setCountry(country);
            emission.setYear(year);
            emission.setCo2Emissions(co2Emissions);
            emission.setSource(source != null && !source.trim().isEmpty() ? source : "N/A");

            emissionService.addEmission(emission, authentication.getName());

            redirectAttributes.addFlashAttribute("success",
                    "Daten für " + country + " (" + year + ") erfolgreich hinzugefügt!");
            return "redirect:/data/my-data";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Fehler beim Speichern: " + e.getMessage());
            return "redirect:/data/add";
        }
    }
}