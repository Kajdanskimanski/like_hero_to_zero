package org.example.like_hero_to_zero.repository;

import org.example.like_hero_to_zero.domain.Emission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für den Datenbankzugriff auf Emissionsdaten.
 * Erweitert JpaRepository um anwendungsspezifische Abfragen.
 * Nur Einträge mit Status "APPROVED" sind öffentlich sichtbar;
 * "PENDING"-Einträge sind ausschließlich für Admins zugänglich.
 */
@Repository
public interface EmissionRepository extends JpaRepository<Emission, Long> {

    // Alle Einträge eines Landes, neueste zuerst (wird für Diagramme genutzt)
    List<Emission> findByCountryOrderByYearDesc(String country);

    // Neuester Eintrag eines Landes (z.B. für Ländervergleich-Diagramm)
    Optional<Emission> findFirstByCountryOrderByYearDesc(String country);

    // Nur Länder mit mindestens einem freigegebenen Eintrag
    @Query("SELECT DISTINCT e.country FROM Emission e WHERE e.status = 'APPROVED' ORDER BY e.country")
    List<String> findAllCountries();

    // Nur Jahre mit mindestens einem freigegebenen Eintrag
    @Query("SELECT DISTINCT e.year FROM Emission e WHERE e.status = 'APPROVED' ORDER BY e.year")
    List<Integer> findAvailableYears();

    // Nur Quellen mit mindestens einem freigegebenen Eintrag
    @Query("SELECT DISTINCT e.source FROM Emission e WHERE e.status = 'APPROVED' ORDER BY e.source")
    List<String> findAvailableSources();

    // Eigene Einträge eines Nutzers, neueste zuerst (für "Meine Daten"-Ansicht)
    List<Emission> findByCreatedByOrderByCreatedAtDesc(String username);
    List<Emission> findByCreatedByOrderByYearDesc(String createdBy);

    // Alle vorhandenen Quellen (inkl. nicht freigegebener) – für Admin-Filter
    @Query("SELECT DISTINCT e.source FROM Emission e WHERE e.source IS NOT NULL ORDER BY e.source")
    List<String> findAllSources();

    // Frühestes und spätestes Jahr im Datensatz – für Jahresbereichs-Filter
    @Query("SELECT MIN(e.year) FROM Emission e")
    Integer findMinYear();

    @Query("SELECT MAX(e.year) FROM Emission e")
    Integer findMaxYear();

    // Alle Einträge, die auf Admin-Freigabe warten, neueste zuerst
    @Query("SELECT e FROM Emission e WHERE e.status = 'PENDING' ORDER BY e.createdAt DESC")
    List<Emission> findPendingEmissions();

    // Länder gefiltert nach Quelle – für dynamische Dropdown-Befüllung im Frontend
    @Query("SELECT DISTINCT e.country FROM Emission e WHERE e.source = :source AND e.status = 'APPROVED' ORDER BY e.country")
    List<String> findCountriesBySource(@Param("source") String source);

    // Jahre gefiltert nach Quelle und Land – für dynamische Dropdown-Befüllung im Frontend
    @Query("SELECT DISTINCT e.year FROM Emission e WHERE e.source = :source AND e.country = :country AND e.status = 'APPROVED' ORDER BY e.year")
    List<Integer> findYearsBySourceAndCountry(@Param("source") String source, @Param("country") String country);

    // Öffentliche Suche: gibt nur freigegebene Einträge zurück (status IS NULL als Fallback für Altdaten)
    @Query("SELECT e FROM Emission e WHERE e.country = :country AND e.year = :year AND e.source = :source AND (e.status = 'APPROVED' OR e.status IS NULL)")
    Optional<Emission> findApprovedEmission(@Param("country") String country,
                                            @Param("year") int year,
                                            @Param("source") String source);

    // Gefilterte globale Daten (nur Systemimporte); alle Parameter optional –
    // null-Werte werden in der Query ignoriert und schränken die Ergebnisse nicht ein
    @Query("SELECT e FROM Emission e WHERE e.createdBy = 'system' " +
            "AND (:country IS NULL OR e.country = :country) " +
            "AND (:source IS NULL OR e.source = :source) " +
            "AND (:yearFrom IS NULL OR e.year >= :yearFrom) " +
            "AND (:yearTo IS NULL OR e.year <= :yearTo) " +
            "ORDER BY e.year DESC, e.country ASC")
    List<Emission> findFilteredGlobalData(@Param("country") String country,
                                          @Param("source") String source,
                                          @Param("yearFrom") Integer yearFrom,
                                          @Param("yearTo") Integer yearTo);

    // Exakte Suche nach Land, Jahr und Quelle (alle dataTypes eines Eintrags)
    List<Emission> findByCountryAndYearAndSource(String country, Integer year, String source);
}