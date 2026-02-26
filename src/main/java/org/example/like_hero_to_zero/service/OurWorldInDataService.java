package org.example.like_hero_to_zero.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.like_hero_to_zero.domain.Emission;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Service
public class OurWorldInDataService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Emission> fetchFromOWID(String countryName) {
        String url = "https://github.com/owid/co2-data/raw/master/owid-co2-data.csv";
        String csv = restTemplate.getForObject(url, String.class);

        List<Emission> result = new ArrayList<>();
        if (csv == null) return result;

        String[] lines = csv.split("\n");
        String[] headers = lines[0].split(",");

        // Spalten-Indizes finden
        int countryIdx = indexOf(headers, "country");
        int yearIdx    = indexOf(headers, "year");
        int co2Idx     = indexOf(headers, "co2");           // Gesamt MtCO2
        int perCapIdx  = indexOf(headers, "co2_per_capita");

        for (int i = 1; i < lines.length; i++) {
            String[] cols = lines[i].split(",");
            if (cols.length <= co2Idx) continue;
            if (!cols[countryIdx].equalsIgnoreCase(countryName)) continue;

            try {
                int year = Integer.parseInt(cols[yearIdx].trim());

                // Gesamt-CO2
                if (!cols[co2Idx].isBlank()) {
                    Emission e = new Emission();
                    e.setCountry(cols[countryIdx].trim());
                    e.setYear(year);
                    e.setCo2Emissions(Double.parseDouble(cols[co2Idx].trim()));
                    e.setDataType("TOTAL");
                    e.setUnit("MtCO2");
                    e.setSource("Our World in Data");
                    e.setCreatedBy("system");
                    e.setIsEditable(false);
                    result.add(e);
                }

                // Pro Kopf
                if (!cols[perCapIdx].isBlank()) {
                    Emission e = new Emission();
                    e.setCountry(cols[countryIdx].trim());
                    e.setYear(year);
                    e.setCo2Emissions(Double.parseDouble(cols[perCapIdx].trim()));
                    e.setDataType("PER_CAPITA");
                    e.setUnit("t per capita");
                    e.setSource("Our World in Data");
                    e.setCreatedBy("system");
                    e.setIsEditable(false);
                    result.add(e);
                }
            } catch (NumberFormatException ignored) {}
        }
        return result;
    }

    private int indexOf(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++)
            if (headers[i].trim().equalsIgnoreCase(name)) return i;
        return -1;
    }
}