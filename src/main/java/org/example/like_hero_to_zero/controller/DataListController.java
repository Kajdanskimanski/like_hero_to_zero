package org.example.like_hero_to_zero.controller;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.service.EmissionService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller für die Datenverwaltung von Emissionseinträgen.
 * Stellt Ansichten für eigene und globale Daten bereit und
 * ermöglicht das Bearbeiten und Löschen eigener Einträge.
 */
@Controller
@RequestMapping("/data")
public class DataListController {

    private final EmissionService emissionService;

    public DataListController(EmissionService emissionService) {
        this.emissionService = emissionService;
    }

    /**
     * Zeigt alle Emissionseinträge des eingeloggten Nutzers an.
     */
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

    /**
     * Zeigt alle freigegebenen globalen Emissionsdaten mit optionalen Filterparametern.
     * Filter: Land, Quelle, Jahreszeitraum (von/bis).
     * Zusätzlich werden alle verfügbaren Filter-Optionen ans Template übergeben,
     * damit Dropdowns und Felder vorausgefüllt werden können.
     */
    @GetMapping("/global-data")
    public String showGlobalData(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            Model model,
            Authentication authentication) {

        List<Emission> globalEmissions = emissionService.getFilteredGlobalData(
                country, source, yearFrom, yearTo
        );

        // Filter-Optionen für die Dropdown-Menüs im Template laden
        List<String> countries = emissionService.getAllCountries();
        List<String> sources = emissionService.getAllSources();
        Integer minYear = emissionService.getMinYear();
        Integer maxYear = emissionService.getMaxYear();

        model.addAttribute("username", authentication.getName());
        model.addAttribute("emissions", globalEmissions);
        model.addAttribute("totalRecords", globalEmissions.size());
        model.addAttribute("viewType", "global");
        model.addAttribute("countries", countries);
        model.addAttribute("sources", sources);
        model.addAttribute("minYear", minYear);
        model.addAttribute("maxYear", maxYear);

        // Aktive Filterwerte zurück ans Template geben, damit Formularfelder befüllt bleiben
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedSource", source);
        model.addAttribute("selectedYearFrom", yearFrom);
        model.addAttribute("selectedYearTo", yearTo);

        return "data-list";
    }

    /**
     * Löscht einen Emissionseintrag anhand seiner ID.
     * Die Service-Schicht prüft, ob der Nutzer berechtigt ist, diesen Eintrag zu löschen.
     */
    @PostMapping("/delete/{id}")
    public String deleteEmission(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            emissionService.deleteEmission(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Datensatz erfolgreich gelöscht!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler: " + e.getMessage());
        }
        return "redirect:/data/my-data";
    }

    /**
     * Zeigt das Bearbeitungsformular für einen vorhandenen Emissionseintrag.
     * Zugriff wird verweigert, wenn der Nutzer nicht der Eigentümer des Eintrags ist.
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {

        String username = authentication.getName();

        // Berechtigungsprüfung vor dem Laden der Daten
        if (!emissionService.canEdit(id, username)) {
            redirectAttributes.addFlashAttribute("error",
                    "Sie haben keine Berechtigung, diese Daten zu bearbeiten!");
            return "redirect:/data/my-data";
        }

        Emission emission = emissionService.getEmissionById(id);

        model.addAttribute("username", username);
        model.addAttribute("emission", emission);
        return "data-edit";
    }

    /**
     * Speichert die Änderungen eines bearbeiteten Emissionseintrags.
     * Felder ohne Eingabe werden mit Standardwerten belegt ("N/A" bzw. "TOTAL").
     * Die Berechtigungsprüfung erfolgt in der Service-Schicht.
     */
    @PostMapping("/edit/{id}")
    public String updateEmission(
            @PathVariable Long id,
            @RequestParam String country,
            @RequestParam Integer year,
            @RequestParam Double co2Emissions,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) Double perCapita,
            @RequestParam(required = false) Double shareGlobal,
            @RequestParam(required = false) Long population,
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
            Emission updatedData = new Emission();
            updatedData.setCountry(country);
            updatedData.setYear(year);
            updatedData.setCo2Emissions(co2Emissions);
            updatedData.setSource(source != null && !source.trim().isEmpty() ? source : "N/A");
            updatedData.setDataType(dataType != null && !dataType.trim().isEmpty() ? dataType : "TOTAL");
            updatedData.setPerCapita(perCapita);
            updatedData.setShareGlobal(shareGlobal);
            updatedData.setPopulation(population);
            updatedData.setCoal(coal);
            updatedData.setOil(oil);
            updatedData.setGas(gas);
            updatedData.setCement(cement);
            updatedData.setFlaring(flaring);
            updatedData.setSolidFuel(solidFuel);
            updatedData.setLiquidFuel(liquidFuel);
            updatedData.setGasFuel(gasFuel);

            emissionService.updateEmission(id, updatedData, authentication.getName());

            redirectAttributes.addFlashAttribute("success", "Datensatz erfolgreich aktualisiert!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Fehler: " + e.getMessage());
        }
        return "redirect:/data/my-data";
    }
}