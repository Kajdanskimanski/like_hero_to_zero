package org.example.like_hero_to_zero.config;

import org.example.like_hero_to_zero.domain.Emission;
import org.example.like_hero_to_zero.domain.User;
import org.example.like_hero_to_zero.repository.EmissionRepository;
import org.example.like_hero_to_zero.repository.UserRepository;
import org.example.like_hero_to_zero.service.OwidApiService;
import org.example.like_hero_to_zero.service.WorldBankApiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(EmissionRepository emissionRepository,
                                   WorldBankApiService apiService,
                                   OwidApiService owidApiService,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {

            // Admin anlegen falls nicht vorhanden
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User("admin", passwordEncoder.encode("admin123"), "ROLE_ADMIN");
                userRepository.save(admin);
                System.out.println("✅ Admin-User angelegt (admin / admin123)");
            }

            if (emissionRepository.count() == 0) {

            // ── World Bank ──────────────────────────────
            System.out.println("🌍 Lade World Bank Daten...");
            List<String> wbCodes = Arrays.asList(
                    "USA","CHN","FRA","GBR","IND","JPN",
                    "CAN","AUS","BRA","RUS","ITA","ESP","MEX","ZAF"
            );
            int total = 0;
            for (String code : wbCodes) {
                try {
                    List<Emission> emissions = apiService.fetchCO2DataForCountry(code);
                    emissions.forEach(emissionRepository::save);
                    total += emissions.size();
                    System.out.println("✅ WB " + code + ": " + emissions.size() + " Datensätze");
                    Thread.sleep(500);
                } catch (Exception e) {
                    System.err.println("❌ Fehler WB " + code + ": " + e.getMessage());
                }
            }

            // ── Our World in Data ───────────────────────
            System.out.println("🌍 Lade Our World in Data...");
            List<String> owidCountries = Arrays.asList(
                    "United States","China","France","United Kingdom",
                    "India","Japan","Canada","Australia","Brazil",
                    "Russia","Italy","Spain","Mexico","South Africa"
            );
            List<Emission> owidData = owidApiService.fetchAllCountries(owidCountries);
            owidData.forEach(emissionRepository::save);
            total += owidData.size();

            System.out.println("🎉 Insgesamt " + total + " Datensätze geladen!");

        } else {
            System.out.println("ℹ️ Datenbank enthält bereits " + emissionRepository.count() + " Datensätze");
        }
        };
    }
}