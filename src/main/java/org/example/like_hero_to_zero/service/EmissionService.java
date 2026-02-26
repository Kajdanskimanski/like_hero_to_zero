package org.example.like_hero_to_zero.service;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.repository.EmissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmissionService {

    private final EmissionRepository emissionRepository;

    public EmissionService(EmissionRepository emissionRepository) {
        this.emissionRepository = emissionRepository;
    }

    // Alle Daten (öffentlich)
    public List<Emission> getAllEmissions() {
        return emissionRepository.findAll();
    }

    // NEU: Nur eigene Daten eines Users
    public List<Emission> getMyEmissions(String username) {
        return emissionRepository.findByCreatedByOrderByCreatedAtDesc(username);
    }

    // NEU: Globale/System-Daten
    public List<Emission> getSystemEmissions() {
        return emissionRepository.findByCreatedByOrderByYearDesc("system");
    }

    public List<Emission> getEmissionsByCountry(String country) {
        return emissionRepository.findByCountryOrderByYearDesc(country);
    }

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

    // NEU: Daten mit User hinzufügen
    public Emission addEmission(Emission emissionData, String username) {
        emissionData.setCreatedBy(username);
        emissionData.setIsEditable(true);
        emissionData.setStatus("PENDING");
        emissionData.setCreatedAt(LocalDateTime.now());
        emissionData.setUpdatedAt(LocalDateTime.now());
        return emissionRepository.save(emissionData);
    }

    // System-Daten hinzufügen (von API)
    public Emission addSystemEmission(Emission emissionData) {
        emissionData.setCreatedBy("system");
        emissionData.setIsEditable(false);
        return emissionRepository.save(emissionData);
    }

    // NEU: Prüfen ob User berechtigt ist
    public boolean canEdit(Long id, String username) {
        Optional<Emission> emission = emissionRepository.findById(id);
        return emission.isPresent() &&
                emission.get().getIsEditable() &&
                emission.get().getCreatedBy().equals(username);
    }

    public Emission updateEmission(Long id, Emission updatedData, String username) {
        return emissionRepository.findById(id)
                .filter(e -> e.getCreatedBy().equals(username)) // Nur eigene!
                .map(existing -> {
                    existing.setCountry(updatedData.getCountry());
                    existing.setYear(updatedData.getYear());
                    existing.setCo2Emissions(updatedData.getCo2Emissions());
                    existing.setSource(updatedData.getSource());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return emissionRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Keine Berechtigung oder Daten nicht gefunden"));
    }

    public void deleteEmission(Long id, String username) {
        emissionRepository.findById(id)
                .filter(e -> e.getCreatedBy().equals(username)) // Nur eigene!
                .ifPresentOrElse(
                        emissionRepository::delete,
                        () -> { throw new RuntimeException("Keine Berechtigung"); }
                );
    }

    public boolean existsByCountryAndYear(String country, Integer year) {
        return emissionRepository.findByCountryOrderByYearDesc(country)
                .stream()
                .anyMatch(data -> data.getYear().equals(year));
    }

    public List<String> getAllSources() {
        return emissionRepository.findAllSources();
    }

    public Integer getMinYear() {
        Integer min = emissionRepository.findMinYear();
        return min != null ? min : 1960;
    }

    public Integer getMaxYear() {
        Integer max = emissionRepository.findMaxYear();
        return max != null ? max : 2023;
    }

    public List<Emission> getFilteredGlobalData(String country, String source,
                                                Integer yearFrom, Integer yearTo) {
        return emissionRepository.findFilteredGlobalData(country, source, yearFrom, yearTo);
    }

    public Optional<Emission> getPublicEmission(String country, int year, String source) {
        return emissionRepository.findApprovedEmission(country, year, source);
    }

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