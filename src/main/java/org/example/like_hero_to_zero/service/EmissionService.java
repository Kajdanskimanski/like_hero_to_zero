package org.example.like_hero_to_zero.service;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.repository.EmissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service-Schicht für die Verwaltung von Emissionsdaten.
 * Enthält die gesamte Geschäftslogik – Controller delegieren hierher,
 * direkte Repository-Zugriffe aus Controllern werden vermieden.
 *
 * Berechtigungskonzept:
 * - Nutzer dürfen nur eigene Einträge bearbeiten/löschen (Prüfung via createdBy)
 * - Systemdaten (createdBy="system") sind grundsätzlich nicht editierbar
 * - Neue Nutzereinträge starten immer als "PENDING" und müssen vom Admin freigegeben werden
 */
@Service
public class EmissionService {

    private final EmissionRepository emissionRepository;

    public EmissionService(EmissionRepository emissionRepository) {
        this.emissionRepository = emissionRepository;
    }

    public List<Emission> getAllEmissions() {
        return emissionRepository.findAll();
    }

    // Eigene Einträge des Nutzers, neueste zuerst
    public List<Emission> getMyEmissions(String username) {
        return emissionRepository.findByCreatedByOrderByCreatedAtDesc(username);
    }

    // Nur systemseitig importierte Daten (createdBy="system")
    public List<Emission> getSystemEmissions() {
        return emissionRepository.findByCreatedByOrderByYearDesc("system");
    }

    public List<Emission> getEmissionsByCountry(String country) {
        return emissionRepository.findByCountryOrderByYearDesc(country);
    }

    // Neuester Eintrag eines Landes – wird z.B. im Ländervergleich-Diagramm verwendet
    public Optional<Emission> getLatestEmissionByCountry(String country) {
        return emissionRepository.findFirstByCountryOrderByYearDesc(country);
    }

    public List<String> getAllCountries() {
        return emissionRepository.findAllCountries();
    }

    public List<Integer> getAvailableYears() {
        return emissionRepository.findAvailableYears();
    }

    public List<String> getAvailableSources() {
        return emissionRepository.findAvailableSources();
    }

    /**
     * Speichert einen neuen Emissionseintrag eines Nutzers.
     * Der Eintrag wird dem Nutzer zugeordnet und auf "PENDING" gesetzt,
     * bis ein Admin ihn freigibt.
     */
    public Emission addEmission(Emission emissionData, String username) {
        emissionData.setCreatedBy(username);
        emissionData.setIsEditable(true);
        emissionData.setStatus("PENDING");
        emissionData.setCreatedAt(LocalDateTime.now());
        emissionData.setUpdatedAt(LocalDateTime.now());
        return emissionRepository.save(emissionData);
    }

    /**
     * Speichert einen systemseitig importierten Eintrag.
     * Diese Einträge sind für Nutzer nicht editierbar.
     */
    public Emission addSystemEmission(Emission emissionData) {
        emissionData.setCreatedBy("system");
        emissionData.setIsEditable(false);
        return emissionRepository.save(emissionData);
    }

    /**
     * Prüft, ob ein Nutzer einen bestimmten Eintrag bearbeiten darf.
     * Bedingungen: Eintrag existiert, ist editierbar, und gehört dem Nutzer.
     */
    public boolean canEdit(Long id, String username) {
        Optional<Emission> emission = emissionRepository.findById(id);
        return emission.isPresent() &&
                emission.get().getIsEditable() &&
                emission.get().getCreatedBy().equals(username);
    }

    public Emission getEmissionById(Long id) {
        return emissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Datensatz nicht gefunden: " + id));
    }

    /**
     * Aktualisiert alle Felder eines vorhandenen Eintrags.
     * Wirft eine Exception wenn der Nutzer nicht der Eigentümer ist (Berechtigungsprüfung via filter).
     */
    public Emission updateEmission(Long id, Emission updatedData, String username) {
        return emissionRepository.findById(id)
                .filter(e -> e.getCreatedBy().equals(username))
                .map(existing -> {
                    existing.setCountry(updatedData.getCountry());
                    existing.setYear(updatedData.getYear());
                    existing.setCo2Emissions(updatedData.getCo2Emissions());
                    existing.setSource(updatedData.getSource());
                    existing.setDataType(updatedData.getDataType());
                    existing.setPerCapita(updatedData.getPerCapita());
                    existing.setShareGlobal(updatedData.getShareGlobal());
                    existing.setPopulation(updatedData.getPopulation());
                    existing.setCoal(updatedData.getCoal());
                    existing.setOil(updatedData.getOil());
                    existing.setGas(updatedData.getGas());
                    existing.setCement(updatedData.getCement());
                    existing.setFlaring(updatedData.getFlaring());
                    existing.setSolidFuel(updatedData.getSolidFuel());
                    existing.setLiquidFuel(updatedData.getLiquidFuel());
                    existing.setGasFuel(updatedData.getGasFuel());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return emissionRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Keine Berechtigung oder Daten nicht gefunden"));
    }

    /**
     * Löscht einen Eintrag – nur wenn der Nutzer der Eigentümer ist.
     * Wirft eine Exception bei fehlender Berechtigung.
     */
    public void deleteEmission(Long id, String username) {
        emissionRepository.findById(id)
                .filter(e -> e.getCreatedBy().equals(username))
                .ifPresentOrElse(
                        emissionRepository::delete,
                        () -> { throw new RuntimeException("Keine Berechtigung"); }
                );
    }

    // Hilfsmethode zur Duplikatprüfung beim Import
    public boolean existsByCountryAndYear(String country, Integer year) {
        return emissionRepository.findByCountryOrderByYearDesc(country)
                .stream()
                .anyMatch(data -> data.getYear().equals(year));
    }

    public List<String> getAllSources() {
        return emissionRepository.findAllSources();
    }

    // Fallback-Werte falls die Datenbank leer ist
    public Integer getMinYear() {
        Integer min = emissionRepository.findMinYear();
        return min != null ? min : 2010;
    }

    public Integer getMaxYear() {
        Integer max = emissionRepository.findMaxYear();
        return max != null ? max : 2025;
    }

    public List<Emission> getFilteredGlobalData(String country, String source,
                                                Integer yearFrom, Integer yearTo) {
        return emissionRepository.findFilteredGlobalData(country, source, yearFrom, yearTo);
    }

    // Nur freigegebene Einträge für die öffentliche Suche
    public Optional<Emission> getPublicEmission(String country, int year, String source) {
        return emissionRepository.findApprovedEmission(country, year, source);
    }

    // Für dynamische Dropdown-Befüllung im Frontend per JavaScript
    public List<String> getCountriesBySource(String source) {
        return emissionRepository.findCountriesBySource(source);
    }

    public List<Integer> getYearsBySourceAndCountry(String source, String country) {
        return emissionRepository.findYearsBySourceAndCountry(source, country);
    }

    public List<Emission> getEmissionsByCountryYearSource(String country, int year, String source) {
        return emissionRepository.findByCountryAndYearAndSource(country, year, source);
    }
}