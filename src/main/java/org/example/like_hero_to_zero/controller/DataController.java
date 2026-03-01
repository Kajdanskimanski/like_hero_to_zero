package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.service.EmissionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller für die manuelle Dateneingabe von Emissionswerten.
 * Nutzer können neue Emissionseinträge erfassen, die anschließend
 * zur Admin-Prüfung als "PENDING" gespeichert werden.
 */
@Controller
@RequestMapping("/data")
public class DataController {

    private final EmissionService emissionService;

    public DataController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    // ── ADD ─────────────────────────────────────────────────────────────────

    /** Zeigt das Formular zur Eingabe neuer Emissionsdaten an. */
    @GetMapping("/add")
    public String showAddForm(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "data-add";
    }

    /**
     * Verarbeitet das ausgefüllte Eingabeformular.
     * Pflichtfelder sind Land, Jahr und CO2-Wert; alle weiteren Felder sind optional.
     * Bei Erfolg wird der Nutzer zu seiner Datenübersicht weitergeleitet,
     * bei einem Fehler zurück zum Formular mit einer Fehlermeldung.
     */
    @PostMapping("/add")
    public String addData(
            // Pflichtfelder
            @RequestParam String country,
            @RequestParam Integer year,
            @RequestParam Double co2Emissions,

            // Metadaten – werden auf Standardwerte gesetzt falls leer (siehe applyFields)
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String dataType,

            // Optionale Zusatzfelder für erweiterte Auswertungen
            @RequestParam(required = false) Double perCapita,
            @RequestParam(required = false) Double shareGlobal,
            @RequestParam(required = false) Long population,

            // Aufschlüsselung nach Emissionsquellen
            @RequestParam(required = false) Double coal,
            @RequestParam(required = false) Double oil,
            @RequestParam(required = false) Double gas,
            @RequestParam(required = false) Double cement,
            @RequestParam(required = false) Double flaring,
            @RequestParam(required = false) Double solidFuel,
            @RequestParam(required = false) Double liquidFuel,
            @RequestParam(required = false) Double gasFuel,

            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            Emission emission = new Emission();
            applyFields(emission, country, year, co2Emissions, source, dataType,
                    perCapita, shareGlobal, population,
                    coal, oil, gas, cement, flaring, solidFuel, liquidFuel, gasFuel);

            // Emission wird dem eingeloggten Nutzer zugeordnet und zur Prüfung eingereicht
            emissionService.addEmission(emission, authentication.getName());

            redirectAttributes.addFlashAttribute("success",
                    "Daten für " + country + " (" + year + ") erfolgreich hinzugefügt!");
            return "redirect:/data/my-data";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler beim Speichern: " + e.getMessage());
            return "redirect:/data/add";
        }
    }

    // ── Hilfsmethode ────────────────────────────────────────────────────────

    /**
     * Überträgt alle Formularwerte auf ein Emission-Objekt.
     * Felder ohne Eingabe werden mit sinnvollen Standardwerten belegt:
     * "source" → "N/A", "dataType" → "TOTAL"
     */
    private void applyFields(Emission emission,
                             String country, Integer year, Double co2Emissions,
                             String source, String dataType,
                             Double perCapita, Double shareGlobal, Long population,
                             Double coal, Double oil, Double gas, Double cement,
                             Double flaring, Double solidFuel, Double liquidFuel, Double gasFuel) {
        emission.setCountry(country);
        emission.setYear(year);
        emission.setCo2Emissions(co2Emissions);
        emission.setSource(source != null && !source.trim().isEmpty() ? source : "N/A");
        emission.setDataType(dataType != null && !dataType.trim().isEmpty() ? dataType : "TOTAL");
        emission.setPerCapita(perCapita);
        emission.setShareGlobal(shareGlobal);
        emission.setPopulation(population);
        emission.setCoal(coal);
        emission.setOil(oil);
        emission.setGas(gas);
        emission.setCement(cement);
        emission.setFlaring(flaring);
        emission.setSolidFuel(solidFuel);
        emission.setLiquidFuel(liquidFuel);
        emission.setGasFuel(gasFuel);
    }
}