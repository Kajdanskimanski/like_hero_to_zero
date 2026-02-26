package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.service.EmissionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class PublicController {

    private final EmissionService emissionService;

    public PublicController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("countries", emissionService.getAllCountries());
        model.addAttribute("years", emissionService.getAvailableYears());
        model.addAttribute("sources", emissionService.getAvailableSources());
        return "public";
    }

    @GetMapping("/search")
    public String search(@RequestParam String country,
                         @RequestParam int year,
                         @RequestParam String source,
                         Model model) {

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

// Gesamtwert
        emissions.stream()
                .filter(e -> "TOTAL".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("totalEmission", e));

// Fallback für alte Einträge ohne dataType
        if (model.getAttribute("totalEmission") == null) {
            model.addAttribute("totalEmission", emissions.get(0));
        }

// Pro Kopf
        emissions.stream()
                .filter(e -> "PER_CAPITA".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("perCapita", e));

// Weltanteil
        emissions.stream()
                .filter(e -> "SHARE_GLOBAL".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("shareGlobal", e));

// Bevölkerung
        emissions.stream()
                .filter(e -> "POPULATION".equals(e.getDataType()))
                .findFirst()
                .ifPresent(e -> model.addAttribute("population", e));

// Sektoren
        List<Emission> sectors = emissions.stream()
                .filter(e -> e.getDataType() != null && List.of("COAL", "OIL", "GAS", "CEMENT", "FLARING",
                                "SOLID_FUEL", "LIQUID_FUEL", "GAS_FUEL")
                        .contains(e.getDataType()))
                .toList();
        model.addAttribute("sectors", sectors);

        return "public";
    }

    @GetMapping("/api/countries")
    @ResponseBody
    public List<String> getCountriesBySource(@RequestParam String source) {
        return emissionService.getCountriesBySource(source);
    }

    @GetMapping("/api/years")
    @ResponseBody
    public List<Integer> getYearsBySourceAndCountry(@RequestParam String source,
                                                    @RequestParam String country) {
        return emissionService.getYearsBySourceAndCountry(source, country);
    }
}