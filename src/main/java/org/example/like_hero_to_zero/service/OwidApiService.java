package org.example.like_hero_to_zero.service;

import org.example.like_hero_to_zero.domain.Emission;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class OwidApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String CSV_URL =
            "https://raw.githubusercontent.com/owid/co2-data/master/owid-co2-data.csv";

    public List<Emission> fetchAllCountries(List<String> countryNames) {
        List<Emission> all = new ArrayList<>();
        try {
            System.out.println("📥 Lade OWID CSV...");
            String csv = restTemplate.getForObject(CSV_URL, String.class);
            System.out.println("✅ CSV geladen, parse Daten...");

            for (String country : countryNames) {
                List<Emission> emissions = parseCsvForCountry(csv, country);
                all.addAll(emissions);
                System.out.println("✅ OWID " + country + ": " + emissions.size() + " Datensätze");
            }
        } catch (Exception e) {
            System.err.println("❌ Fehler OWID: " + e.getMessage());
        }
        return all;
    }

    private List<Emission> parseCsvForCountry(String csv, String countryName) {
        List<Emission> emissions = new ArrayList<>();
        String[] lines = csv.split("\n");
        String[] headers = lines[0].split(",");

        // Alle relevanten Spalten suchen
        int countryIdx    = indexOf(headers, "country");
        int yearIdx       = indexOf(headers, "year");
        int co2Idx        = indexOf(headers, "co2");                    // Gesamt MtCO2
        int perCapitaIdx  = indexOf(headers, "co2_per_capita");         // t pro Person
        int coalIdx       = indexOf(headers, "coal_co2");               // aus Kohle
        int oilIdx        = indexOf(headers, "oil_co2");                // aus Öl
        int gasIdx        = indexOf(headers, "gas_co2");                // aus Gas
        int cementIdx     = indexOf(headers, "cement_co2");             // aus Zement
        int flareIdx      = indexOf(headers, "flaring_co2");            // aus Gas-Flaring
        int shareWorldIdx = indexOf(headers, "share_global_co2");       // % der Weltemissionen
        int gdpIdx        = indexOf(headers, "gdp");                    // BIP (für CO2/BIP)
        int populationIdx = indexOf(headers, "population");             // Bevölkerung

        if (countryIdx == -1 || yearIdx == -1 || co2Idx == -1) {
            System.err.println("❌ CSV-Header nicht gefunden!");
            return emissions;
        }

        for (int i = 1; i < lines.length; i++) {
            String[] cols = lines[i].split(",", -1);
            if (cols.length <= co2Idx) continue;

            String country = cols[countryIdx].replace("\"", "").trim();
            if (!country.equalsIgnoreCase(countryName)) continue;

            try {
                int year = Integer.parseInt(cols[yearIdx].trim());
                if (year < 1990 || year > 2022) continue;

                // Gesamt-CO2 (Pflichtfeld)
                String co2Str = cols[co2Idx].trim();
                if (co2Str.isEmpty()) continue;
                double co2 = Double.parseDouble(co2Str);

                // Haupteintrag
                Emission main = buildEmission(country, year, co2, "TOTAL", "MtCO2");
                emissions.add(main);

                // Pro Kopf
                addIfPresent(emissions, cols, perCapitaIdx, country, year, "PER_CAPITA", "t per capita");

                // Sektoren
                addIfPresent(emissions, cols, coalIdx,    country, year, "COAL",    "MtCO2");
                addIfPresent(emissions, cols, oilIdx,     country, year, "OIL",     "MtCO2");
                addIfPresent(emissions, cols, gasIdx,     country, year, "GAS",     "MtCO2");
                addIfPresent(emissions, cols, cementIdx,  country, year, "CEMENT",  "MtCO2");
                addIfPresent(emissions, cols, flareIdx,   country, year, "FLARING", "MtCO2");

                // Weltanteil & Metadaten
                addIfPresent(emissions, cols, shareWorldIdx, country, year, "SHARE_GLOBAL", "%");
                addIfPresent(emissions, cols, populationIdx, country, year, "POPULATION",   "persons");
                addIfPresent(emissions, cols, gdpIdx,        country, year, "GDP",          "USD");

            } catch (NumberFormatException ignored) {}
        }
        return emissions;
    }

    // Hilfsmethode: Emission anlegen wenn Wert vorhanden
    private void addIfPresent(List<Emission> list, String[] cols, int idx,
                              String country, int year, String dataType, String unit) {
        if (idx == -1 || idx >= cols.length) return;
        String val = cols[idx].trim();
        if (val.isEmpty()) return;
        try {
            list.add(buildEmission(country, year, Double.parseDouble(val), dataType, unit));
        } catch (NumberFormatException ignored) {}
    }

    private Emission buildEmission(String country, int year, double value,
                                   String dataType, String unit) {
        Emission e = new Emission();
        e.setCountry(country);
        e.setYear(year);
        e.setCo2Emissions(value);
        e.setDataType(dataType);   // neues Feld
        e.setUnit(unit);           // neues Feld
        e.setSource("Our World in Data");
        e.setCreatedBy("system");
        e.setIsEditable(false);
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }

    private int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++)
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        return -1;
    }
}