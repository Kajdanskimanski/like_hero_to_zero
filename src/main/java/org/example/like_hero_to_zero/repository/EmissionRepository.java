package org.example.like_hero_to_zero.repository;

import org.example.like_hero_to_zero.domain.Emission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmissionRepository extends JpaRepository<Emission, Long> {

    // Bestehende Methoden...
    List<Emission> findByCountryOrderByYearDesc(String country);

    Optional<Emission> findFirstByCountryOrderByYearDesc(String country);

    @Query("SELECT DISTINCT e.country FROM Emission e WHERE e.status = 'APPROVED' ORDER BY e.country")
    List<String> findAllCountries();

    @Query("SELECT DISTINCT e.year FROM Emission e WHERE e.status = 'APPROVED' ORDER BY e.year")
    List<Integer> findAvailableYears();

    @Query("SELECT DISTINCT e.source FROM Emission e WHERE e.status = 'APPROVED' ORDER BY e.source")
    List<String> findAvailableSources();

    List<Emission> findByCreatedByOrderByCreatedAtDesc(String username);

    List<Emission> findByCreatedByOrderByYearDesc(String createdBy);

    // NEU: Alle verfügbaren Quellen
    @Query("SELECT DISTINCT e.source FROM Emission e WHERE e.source IS NOT NULL ORDER BY e.source")
    List<String> findAllSources();

    // NEU: Jahre-Range
    @Query("SELECT MIN(e.year) FROM Emission e")
    Integer findMinYear();

    @Query("SELECT MAX(e.year) FROM Emission e")
    Integer findMaxYear();

    @Query("SELECT e FROM Emission e WHERE e.status = 'PENDING' ORDER BY e.createdAt DESC")
    List<Emission> findPendingEmissions();

    @Query("SELECT DISTINCT e.country FROM Emission e WHERE e.source = :source AND e.status = 'APPROVED' ORDER BY e.country")
    List<String> findCountriesBySource(@Param("source") String source);

    @Query("SELECT DISTINCT e.year FROM Emission e WHERE e.source = :source AND e.country = :country AND e.status = 'APPROVED' ORDER BY e.year")
    List<Integer> findYearsBySourceAndCountry(@Param("source") String source, @Param("country") String country);

    // Öffentliche Suche nur APPROVED zeigen:
    @Query("SELECT e FROM Emission e WHERE e.country = :country AND e.year = :year AND e.source = :source AND (e.status = 'APPROVED' OR e.status IS NULL)")
    Optional<Emission> findApprovedEmission(@Param("country") String country, @Param("year") int year, @Param("source") String source);

    // NEU: Gefilterte Suche für globale Daten
    @Query("SELECT e FROM Emission e WHERE e.createdBy = 'system' " + "AND (:country IS NULL OR e.country = :country) " + "AND (:source IS NULL OR e.source = :source) " + "AND (:yearFrom IS NULL OR e.year >= :yearFrom) " + "AND (:yearTo IS NULL OR e.year <= :yearTo) " + "ORDER BY e.year DESC, e.country ASC")
    List<Emission> findFilteredGlobalData(@Param("country") String country, @Param("source") String source, @Param("yearFrom") Integer yearFrom, @Param("yearTo") Integer yearTo);

    List<Emission> findByCountryAndYearAndSource(String country, Integer year, String source);

    Optional<Emission> findFirstByCountryAndYearAndSourceAndCreatedBy(String country, Integer year, String source, String createdBy);

    Optional<Emission> findFirstByCountryAndYearAndSource(String country, int year, String source);
}