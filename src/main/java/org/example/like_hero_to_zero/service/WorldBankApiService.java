package org.example.like_hero_to_zero.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.like_hero_to_zero.domain.Emission;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WorldBankApiService {

    private final RestTemplate restTemplate = new RestTemplate();  // ← das fehlt
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String[]> INDICATORS = Map.of(
            "EN.ATM.CO2E.KT",    new String[]{"TOTAL",       "kt"},
            "EN.ATM.CO2E.PC",    new String[]{"PER_CAPITA",  "t per capita"},
            "EN.ATM.CO2E.SF.KT", new String[]{"SOLID_FUEL",  "kt"},
            "EN.ATM.CO2E.LF.KT", new String[]{"LIQUID_FUEL", "kt"},
            "EN.ATM.CO2E.GF.KT", new String[]{"GAS_FUEL",    "kt"}
    );

    public List<Emission> fetchCO2DataForCountry(String countryCode) {
        List<Emission> all = new ArrayList<>();
        for (var entry : INDICATORS.entrySet()) {
            String url = String.format(
                    "https://api.worldbank.org/v2/country/%s/indicator/%s?format=json&per_page=50&date=1990:2023",
                    countryCode, entry.getKey()
            );
            try {
                String response = restTemplate.getForObject(url, String.class);
                all.addAll(parseResponse(response, entry.getValue()[0], entry.getValue()[1]));
            } catch (Exception e) {
                System.err.println("Fehler: " + e.getMessage());
            }
        }
        return all;
    }

    private List<Emission> parseResponse(String json, String dataType, String unit) {
        List<Emission> list = new ArrayList<>();
        try {
            JsonNode data = objectMapper.readTree(json).get(1);
            for (JsonNode item : data) {
                JsonNode valueNode = item.get("value");
                if (!valueNode.isNull()) {
                    Emission e = new Emission();
                    e.setCountry(item.get("country").get("value").asText());
                    e.setYear(item.get("date").asInt());
                    e.setCo2Emissions(valueNode.asDouble());
                    e.setDataType(dataType);   // NEU
                    e.setUnit(unit);           // NEU
                    e.setSource("World Bank");
                    e.setCreatedBy("system");
                    e.setIsEditable(false);
                    list.add(e);
                }
            }
        } catch (Exception e) {
            System.err.println("Parse-Fehler: " + e.getMessage());
        }
        return list;
    }
}