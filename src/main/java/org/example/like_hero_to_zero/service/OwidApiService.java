package org.example.like_hero_to_zero.service;

import jakarta.annotation.PostConstruct;
import org.example.like_hero_to_zero.domain.Emission;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service zum Laden und Parsen von CO2-Daten der OWID-Datenbank (Our World in Data).
 * Die CSV-Datei wird einmalig beim Anwendungsstart gecacht, um wiederholte
 * Netzwerkanfragen zu vermeiden. Alle importierten Einträge werden als
 * Systemdaten markiert (createdBy="system", isEditable=false, status="APPROVED").
 *
 * Datenquelle: https://github.com/owid/co2-data
 */
@Service
public class OwidApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    // CSV wird einmalig beim Start geladen und im Speicher gehalten
    private String cachedCsv = null;

    private static final String CSV_URL =
            "https://raw.githubusercontent.com/owid/co2-data/master/owid-co2-data.csv";

    /**
     * Lädt die OWID-CSV beim Anwendungsstart vor.
     * Wird von Spring automatisch nach der Bean-Initialisierung aufgerufen.
     * Bei Fehler bleibt cachedCsv null – spätere Aufrufe geben dann eine leere Liste zurück.
     */
    @PostConstruct
    public void preloadCsv() {
        try {
            cachedCsv = restTemplate.getForObject(CSV_URL, String.class);
        } catch (Exception e) {
            System.err.println("CSV-Vorladung fehlgeschlagen: " + e.getMessage());
        }
    }

    /**
     * Gibt alle Emissionseinträge für eine Liste von Ländern zurück.
     * Liest aus dem Cache – kein Netzwerkzugriff zur Laufzeit.
     */
    public List<Emission> fetchAllCountries(List<String> countryNames) {
        List<Emission> all = new ArrayList<>();
        if (cachedCsv == null) {
            System.err.println("CSV nicht geladen!");
            return all;
        }
        for (String country : countryNames) {
            all.addAll(parseCsvForCountry(cachedCsv, country));
        }
        return all;
    }

    /**
     * Parst die CSV und extrahiert alle Emissionseinträge für ein bestimmtes Land.
     * Nur Jahre von 1990–2022 werden berücksichtigt. Einträge ohne CO2-Gesamtwert werden übersprungen.
     * Pro Zeile werden mehrere Emission-Objekte erzeugt (je eines pro Datenkategorie/dataType).
     */
    private List<Emission> parseCsvForCountry(String csv, String countryName) {
        List<Emission> emissions = new ArrayList<>();
        String[] lines = csv.split("\n");
        String[] headers = lines[0].split(",");

        // Spaltenindizes anhand der Header-Zeile ermitteln
        int countryIdx    = indexOf(headers, "country");
        int yearIdx       = indexOf(headers, "year");
        int co2Idx        = indexOf(headers, "co2");                // Gesamt-CO2 in MtCO2
        int perCapitaIdx  = indexOf(headers, "co2_per_capita");     // CO2 pro Kopf in t
        int coalIdx       = indexOf(headers, "coal_co2");
        int oilIdx        = indexOf(headers, "oil_co2");
        int gasIdx        = indexOf(headers, "gas_co2");
        int cementIdx     = indexOf(headers, "cement_co2");
        int flareIdx      = indexOf(headers, "flaring_co2");
        int shareWorldIdx = indexOf(headers, "share_global_co2");   // Anteil an Weltemissionen in %
        int gdpIdx        = indexOf(headers, "gdp");
        int populationIdx = indexOf(headers, "population");

        if (countryIdx == -1 || yearIdx == -1 || co2Idx == -1) {
            System.err.println("Pflicht-Header nicht gefunden – CSV-Format möglicherweise geändert!");
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

                // CO2-Gesamtwert ist Pflicht – Zeile überspringen falls leer
                String co2Str = cols[co2Idx].trim();
                if (co2Str.isEmpty()) continue;
                double co2 = Double.parseDouble(co2Str);

                // Haupteintrag mit Gesamtemission
                emissions.add(buildEmission(country, year, co2, "TOTAL", "MtCO2"));

                // Optionale Felder: werden nur hinzugefügt wenn ein Wert vorhanden ist
                addIfPresent(emissions, cols, perCapitaIdx,  country, year, "PER_CAPITA",   "t per capita");
                addIfPresent(emissions, cols, coalIdx,       country, year, "COAL",          "MtCO2");
                addIfPresent(emissions, cols, oilIdx,        country, year, "OIL",           "MtCO2");
                addIfPresent(emissions, cols, gasIdx,        country, year, "GAS",           "MtCO2");
                addIfPresent(emissions, cols, cementIdx,     country, year, "CEMENT",        "MtCO2");
                addIfPresent(emissions, cols, flareIdx,      country, year, "FLARING",       "MtCO2");
                addIfPresent(emissions, cols, shareWorldIdx, country, year, "SHARE_GLOBAL",  "%");
                addIfPresent(emissions, cols, populationIdx, country, year, "POPULATION",    "persons");
                addIfPresent(emissions, cols, gdpIdx,        country, year, "GDP",           "USD");

            } catch (NumberFormatException ignored) {
                // Ungültige Zahlenformate in einzelnen Zeilen werden stillschweigend übersprungen
            }
        }
        return emissions;
    }

    /**
     * Fügt einen Emissionseintrag zur Liste hinzu, falls der Wert in der CSV vorhanden ist.
     * Verhindert leere oder fehlerhafte Einträge in der Datenbank.
     */
    private void addIfPresent(List<Emission> list, String[] cols, int idx,
                              String country, int year, String dataType, String unit) {
        if (idx == -1 || idx >= cols.length) return;
        String val = cols[idx].trim();
        if (val.isEmpty()) return;
        try {
            list.add(buildEmission(country, year, Double.parseDouble(val), dataType, unit));
        } catch (NumberFormatException ignored) {}
    }

    /**
     * Erstellt ein neues Emission-Objekt mit allen Systemattributen.
     * Alle OWID-Importe gelten als sofort freigegeben und nicht editierbar.
     */
    private Emission buildEmission(String country, int year, double value,
                                   String dataType, String unit) {
        Emission e = new Emission();
        e.setCountry(country);
        e.setYear(year);
        e.setStatus("APPROVED");
        e.setCo2Emissions(value);
        e.setDataType(dataType);
        e.setUnit(unit);
        e.setSource("Our World in Data");
        e.setCreatedBy("system");
        e.setIsEditable(false);
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());
        return e;
    }

    /**
     * Sucht einen Spaltennamen in den CSV-Headern (case-insensitive).
     * Gibt -1 zurück wenn die Spalte nicht gefunden wird.
     */
    private int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++)
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        return -1;
    }
}