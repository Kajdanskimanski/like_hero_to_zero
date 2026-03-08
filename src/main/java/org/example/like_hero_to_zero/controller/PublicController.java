package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.service.EmissionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller für den öffentlichen Bereich der Anwendung.
 * Kein Login erforderlich – zeigt Emissionsdaten für alle Besucher an.
 * Enthält außerdem zwei REST-Endpunkte für dynamische Dropdown-Filterung per JavaScript.
 */
@Controller
public class PublicController {

    private final EmissionService emissionService;

    public PublicController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    /**
     * Startseite: Zeigt das Suchformular mit allen verfügbaren
     * Ländern, Jahren und Quellen für die Dropdowns an.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("countries", emissionService.getAllCountries());
        model.addAttribute("years", emissionService.getAvailableYears());
        model.addAttribute("sources", emissionService.getAvailableSources());
        return "public";
    }

    /**
     * Verarbeitet eine Suchanfrage und bereitet die Ergebnisse für die Anzeige auf.
     * Sektordaten werden direkt aus den Feldern des Eintrags gelesen (coal, oil, gas, ...),
     * da sie nicht als separate dataType-Einträge gespeichert sind.
     */
    @GetMapping("/search")
    public String search(@RequestParam String country,
                         @RequestParam int year,
                         @RequestParam String source,
                         Model model) {

        // Dropdown-Optionen erneut laden, damit das Formular nach der Suche befüllt bleibt
        model.addAttribute("countries", emissionService.getAllCountries());
        model.addAttribute("years", emissionService.getAvailableYears());
        model.addAttribute("sources", emissionService.getAvailableSources());
        model.addAttribute("country", country);
        model.addAttribute("year", year);
        model.addAttribute("source", source);

        List<Emission> emissions = emissionService.getEmissionsByCountryYearSource(country, year, source);

        if (emissions.isEmpty()) {
            model.addAttribute("error", "Keine Daten für diese Auswahl gefunden.");
            return "public";
        }

        // Gesamtemission (primärer Anzeigewert)
        emissions.stream()
                .filter(e -> "TOTAL".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("totalEmission", e));

        // Fallback für ältere Einträge, die noch keinen dataType gesetzt haben
        if (model.getAttribute("totalEmission") == null) {
            model.addAttribute("totalEmission", emissions.get(0));
        }

        // Optionale Zusatzwerte – werden nur angezeigt wenn vorhanden
        emissions.stream()
                .filter(e -> "PER_CAPITA".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("perCapita", e));

        emissions.stream()
                .filter(e -> "SHARE_GLOBAL".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("shareGlobal", e));

        emissions.stream()
                .filter(e -> "POPULATION".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("population", e));

        // Sektordaten direkt aus den Feldern des ersten Eintrags lesen.
        // Die Felder coal, oil, gas etc. sind direkt in der Emission-Entity gespeichert
        // und müssen nicht als separate dataType-Einträge vorliegen.
        Emission anyEntry = emissions.get(0);
        List<Map<String, Object>> sectors = new ArrayList<>();
        if (anyEntry.getCoal() != null)       sectors.add(Map.of("name", "Kohle",                 "value", anyEntry.getCoal()));
        if (anyEntry.getOil() != null)        sectors.add(Map.of("name", "Öl",                    "value", anyEntry.getOil()));
        if (anyEntry.getGas() != null)        sectors.add(Map.of("name", "Gas",                   "value", anyEntry.getGas()));
        if (anyEntry.getCement() != null)     sectors.add(Map.of("name", "Zement",                "value", anyEntry.getCement()));
        if (anyEntry.getFlaring() != null)    sectors.add(Map.of("name", "Abfackeln",             "value", anyEntry.getFlaring()));
        if (anyEntry.getSolidFuel() != null)  sectors.add(Map.of("name", "Fester Brennstoff",     "value", anyEntry.getSolidFuel()));
        if (anyEntry.getLiquidFuel() != null) sectors.add(Map.of("name", "Flüssiger Brennstoff",  "value", anyEntry.getLiquidFuel()));
        if (anyEntry.getGasFuel() != null)    sectors.add(Map.of("name", "Gasförmiger Brennstoff","value", anyEntry.getGasFuel()));
        model.addAttribute("sectors", sectors);

        return "public";
    }

    /**
     * REST-Endpunkt: Gibt alle Länder zurück, für die eine bestimmte Quelle Daten hat.
     * Wird per JavaScript aufgerufen, um das Länder-Dropdown dynamisch zu befüllen.
     */
    @GetMapping("/api/countries")
    @ResponseBody
    public List<String> getCountriesBySource(@RequestParam String source) {
        return emissionService.getCountriesBySource(source);
    }

    /**
     * REST-Endpunkt: Gibt alle Jahre zurück, für die Daten einer bestimmten
     * Quelle und eines bestimmten Landes existieren.
     * Wird per JavaScript aufgerufen, um das Jahres-Dropdown dynamisch zu befüllen.
     */
    @GetMapping("/api/years")
    @ResponseBody
    public List<Integer> getYearsBySourceAndCountry(@RequestParam String source,
                                                    @RequestParam String country) {
        return emissionService.getYearsBySourceAndCountry(source, country);
    }
}