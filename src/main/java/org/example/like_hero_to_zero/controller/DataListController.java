package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.service.EmissionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/data")
public class DataListController {

    private final EmissionService emissionService;

    public DataListController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    // Meine Daten anzeigen
    @GetMapping("/my-data")
    public String showMyData(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Emission> myEmissions = emissionService.getMyEmissions(username);

        model.addAttribute("username", username);
        model.addAttribute("emissions", myEmissions);
        model.addAttribute("totalRecords", myEmissions.size());
        model.addAttribute("viewType", "my");

        return "data-list";
    }

    // Globale Daten mit Filtern - NUR DIESE EINE METHODE!
    @GetMapping("/global-data")
    public String showGlobalData(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            Model model,
            Authentication authentication) {

        // Gefilterte Daten abrufen
        List<Emission> globalEmissions = emissionService.getFilteredGlobalData(
                country, source, yearFrom, yearTo
        );

        // Filter-Optionen für Dropdowns
        List<String> countries = emissionService.getAllCountries();
        List<String> sources = emissionService.getAllSources();
        Integer minYear = emissionService.getMinYear();
        Integer maxYear = emissionService.getMaxYear();

        model.addAttribute("username", authentication.getName());
        model.addAttribute("emissions", globalEmissions);
        model.addAttribute("totalRecords", globalEmissions.size());
        model.addAttribute("viewType", "global");

        // Filter-Optionen
        model.addAttribute("countries", countries);
        model.addAttribute("sources", sources);
        model.addAttribute("minYear", minYear);
        model.addAttribute("maxYear", maxYear);

        // Aktuelle Filter-Werte
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedSource", source);
        model.addAttribute("selectedYearFrom", yearFrom);
        model.addAttribute("selectedYearTo", yearTo);

        return "data-list";
    }

    // Daten löschen (nur eigene!)
    @PostMapping("/delete/{id}")
    public String deleteEmission(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            emissionService.deleteEmission(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success",
                    "Datensatz erfolgreich gelöscht!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Fehler: " + e.getMessage());
        }
        return "redirect:/data/my-data";
    }

    // Bearbeiten-Form anzeigen (nur eigene!)
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        if (!emissionService.canEdit(id, username)) {
            redirectAttributes.addFlashAttribute("error",
                    "Sie haben keine Berechtigung, diese Daten zu bearbeiten!");
            return "redirect:/data/my-data";
        }

        Optional<Emission> emission = emissionService.getAllEmissions()
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();

        if (emission.isEmpty()) {
            return "redirect:/data/my-data";
        }

        model.addAttribute("username", username);
        model.addAttribute("emission", emission.get());
        return "data-edit";
    }

    // Daten aktualisieren (nur eigene!)
    @PostMapping("/edit/{id}")
    public String updateEmission(@PathVariable Long id,
                                 @RequestParam String country,
                                 @RequestParam Integer year,
                                 @RequestParam Double co2Emissions,
                                 @RequestParam(required = false) String source,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            Emission updatedData = new Emission();
            updatedData.setCountry(country);
            updatedData.setYear(year);
            updatedData.setCo2Emissions(co2Emissions);
            updatedData.setSource(source != null && !source.trim().isEmpty() ? source : "N/A");

            emissionService.updateEmission(id, updatedData, authentication.getName());

            redirectAttributes.addFlashAttribute("success",
                    "Datensatz erfolgreich aktualisiert!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Fehler: " + e.getMessage());
        }

        return "redirect:/data/my-data";
    }
}