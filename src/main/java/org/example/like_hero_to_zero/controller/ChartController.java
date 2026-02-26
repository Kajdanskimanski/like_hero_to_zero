package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.domain.*;
import org.example.like_hero_to_zero.service.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.*;

@Controller
@RequestMapping("/charts")
public class ChartController {

    private final EmissionService emissionService;

    public ChartController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    @GetMapping
    public String showCharts(@RequestParam(required = false) String country, Model model) {
        List<String> countries = emissionService.getAllCountries();
        model.addAttribute("countries", countries);

        if (country != null && !country.isEmpty()) {
            // Daten für das ausgewählte Land
            List<Emission> emissions = emissionService.getEmissionsByCountry(country);

            if (!emissions.isEmpty()) {
                // Jahre extrahieren (für X-Achse)
                List<Integer> years = emissions.stream()
                        .map(Emission::getYear)
                        .sorted()
                        .collect(Collectors.toList());

                // CO2-Werte extrahieren (für Y-Achse)
                List<Double> values = emissions.stream()
                        .sorted((a, b) -> a.getYear().compareTo(b.getYear()))
                        .map(Emission::getCo2Emissions)
                        .collect(Collectors.toList());

                model.addAttribute("selectedCountry", country);
                model.addAttribute("years", years);
                model.addAttribute("values", values);
                model.addAttribute("hasData", true);
            } else {
                model.addAttribute("error", "Keine Daten für " + country + " gefunden.");
                model.addAttribute("hasData", false);
            }
        } else {
            model.addAttribute("hasData", false);
        }

        return "charts";
    }

    @GetMapping("/comparison")
    public String showComparison(Model model) {
        List<String> countries = emissionService.getAllCountries();

        // Für jeden Länder die neuesten Daten holen
        List<String> countryNames = countries.stream()
                .limit(10) // Nur die ersten 10 Länder für bessere Übersicht
                .collect(Collectors.toList());

        List<Double> latestValues = countryNames.stream()
                .map(country -> emissionService.getLatestEmissionByCountry(country))
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get().getCo2Emissions())
                .collect(Collectors.toList());

        model.addAttribute("countryNames", countryNames);
        model.addAttribute("latestValues", latestValues);

        return "charts-comparison";
    }
}