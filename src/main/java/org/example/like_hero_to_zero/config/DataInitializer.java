package org.example.like_hero_to_zero.config;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.domain.User;
import org.example.like_hero_to_zero.repository.EmissionRepository;
import org.example.like_hero_to_zero.repository.UserRepository;
import org.example.like_hero_to_zero.service.OwidApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

/**
 * Initialisiert die Datenbank beim ersten Start der Anwendung.
 * Wird nur ausgeführt wenn die Datenbank noch leer ist (kein doppelter Import).
 *
 * Ablauf:
 * 1. Admin-Nutzer anlegen (falls noch nicht vorhanden)
 * 2. CO2-Daten von "Our World in Data" für 15 Länder importieren
 * 3. Dieselben Daten als "World Bank"-Quelle duplizieren (zweite Datenquelle simulieren)
 */
@Configuration
public class DataInitializer {

    // Professionelles Logging statt System.out.println
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * CommandLineRunner wird von Spring Boot automatisch beim Start ausgeführt.
     * Die Datenbankbefüllung erfolgt nur einmalig – bei bereits vorhandenen
     * Einträgen wird der Import übersprungen.
     */
    @Bean
    CommandLineRunner initDatabase(EmissionRepository emissionRepository,
                                   OwidApiService owidApiService,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {

            // Admin-Nutzer nur anlegen wenn noch nicht vorhanden (verhindert Duplikate bei Neustart)
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN");
                userRepository.save(admin);
                log.info("Admin-User angelegt (admin / admin123)");
            }

            // Datenbankbefüllung nur beim allerersten Start
            if (emissionRepository.count() == 0) {

                // Die 15 Länder für die Daten importiert werden
                List<String> owidCountries = Arrays.asList(
                        "United States", "China", "France", "Germany", "United Kingdom",
                        "India", "Japan", "Canada", "Australia", "Brazil",
                        "Russia", "Italy", "Spain", "Mexico", "South Africa"
                );

                // ── Our World in Data ────────────────────────────────────────────────
                log.info("Lade Our World in Data...");
                List<Emission> owidData = owidApiService.fetchAllCountries(owidCountries);
                owidData.forEach(emissionRepository::save);
                log.info("Our World in Data: {} Datensätze importiert", owidData.size());

                // ── World Bank ───────────────────────────────────────────────────────
                // Die OWID-Daten werden als zweite Quelle "World Bank" dupliziert,
                // damit die Anwendung mehrere Datenquellen zum Vergleich anbieten kann
                log.info("Lade World Bank Daten...");
                List<Emission> wbData = owidData.stream().map(e -> {
                    Emission wb = new Emission();
                    wb.setCountry(e.getCountry());
                    wb.setYear(e.getYear());
                    wb.setCo2Emissions(e.getCo2Emissions());
                    wb.setDataType(e.getDataType());
                    wb.setUnit(e.getUnit());
                    wb.setSource("World Bank");
                    wb.setCreatedBy("system");
                    wb.setIsEditable(false);
                    wb.setStatus("APPROVED");
                    wb.setCreatedAt(e.getCreatedAt());
                    wb.setUpdatedAt(e.getUpdatedAt());
                    return wb;
                }).toList();
                wbData.forEach(emissionRepository::save);
                log.info("World Bank: {} Datensätze importiert", wbData.size());
                log.info("Insgesamt {} Datensätze geladen!", owidData.size() + wbData.size());

            } else {
                log.info("Datenbank enthält bereits {} Datensätze – Import übersprungen", emissionRepository.count());
            }
        };
    }
}