package org.example.like_hero_to_zero.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA-Entity für einen Emissionsdatensatz.
 * Wird in der Tabelle "emission_data" gespeichert.
 *
 * Status-Werte: "PENDING" (wartet auf Freigabe), "APPROVED" (sichtbar), "REJECTED" (abgelehnt).
 * Systemdaten (importierte Einträge) haben isEditable=false und createdBy="system".
 */
@Entity
@Table(name = "emission_data")
public class Emission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Double co2Emissions;

    private String source;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Neue Nutzereinträge starten als "APPROVED" – manuell eingereichte als "PENDING"
    @Column(name = "status")
    private String status = "APPROVED";

    @Column(name = "created_by")
    private String createdBy;

    // Systemimportierte Daten sind nicht editierbar (isEditable=false)
    @Column(name = "is_editable")
    private Boolean isEditable = true;

    // Kategorisiert den Eintrag (z.B. "TOTAL", "PER_CAPITA", "COAL" ...)
    @Column(name = "data_type")
    private String dataType;

    @Column(name = "unit")
    private String unit;

    // ── Erweiterte öffentliche Felder ────────────────────────────────────────

    @Column(name = "per_capita")
    private Double perCapita;       // CO₂-Ausstoß pro Einwohner

    @Column(name = "share_global")
    private Double shareGlobal;     // Anteil an den globalen Gesamtemissionen in %

    @Column(name = "population")
    private Long population;        // Bevölkerungszahl im jeweiligen Jahr

    // ── Emissionen nach Sektoren ─────────────────────────────────────────────

    @Column(name = "coal")
    private Double coal;            // Kohle

    @Column(name = "oil")
    private Double oil;             // Öl

    @Column(name = "gas")
    private Double gas;             // Gas

    @Column(name = "cement")
    private Double cement;          // Zementproduktion

    @Column(name = "flaring")
    private Double flaring;         // Gasfackelung

    @Column(name = "solid_fuel")
    private Double solidFuel;       // Feste Brennstoffe

    @Column(name = "liquid_fuel")
    private Double liquidFuel;      // Flüssige Brennstoffe

    @Column(name = "gas_fuel")
    private Double gasFuel;         // Gasförmige Brennstoffe

    // ── Konstruktoren ────────────────────────────────────────────────────────

    /**
     * Standard-Konstruktor: Setzt Timestamps automatisch auf den aktuellen Zeitpunkt.
     */
    public Emission() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Konstruktor für systemseitig importierte Datensätze.
     * Setzt createdBy="system" und isEditable=false,
     * damit diese Einträge nicht von Nutzern verändert werden können.
     */
    public Emission(String country, Integer year, Double co2Emissions, String source) {
        this();
        this.country = country;
        this.year = year;
        this.co2Emissions = co2Emissions;
        this.source = source;
        this.createdBy = "system";
        this.isEditable = false;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getCo2Emissions() { return co2Emissions; }
    public void setCo2Emissions(Double co2Emissions) { this.co2Emissions = co2Emissions; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Boolean getIsEditable() { return isEditable; }
    public void setIsEditable(Boolean isEditable) { this.isEditable = isEditable; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getPerCapita() { return perCapita; }
    public void setPerCapita(Double perCapita) { this.perCapita = perCapita; }

    public Double getShareGlobal() { return shareGlobal; }
    public void setShareGlobal(Double shareGlobal) { this.shareGlobal = shareGlobal; }

    public Long getPopulation() { return population; }
    public void setPopulation(Long population) { this.population = population; }

    public Double getCoal() { return coal; }
    public void setCoal(Double coal) { this.coal = coal; }

    public Double getOil() { return oil; }
    public void setOil(Double oil) { this.oil = oil; }

    public Double getGas() { return gas; }
    public void setGas(Double gas) { this.gas = gas; }

    public Double getCement() { return cement; }
    public void setCement(Double cement) { this.cement = cement; }

    public Double getFlaring() { return flaring; }
    public void setFlaring(Double flaring) { this.flaring = flaring; }

    public Double getSolidFuel() { return solidFuel; }
    public void setSolidFuel(Double solidFuel) { this.solidFuel = solidFuel; }

    public Double getLiquidFuel() { return liquidFuel; }
    public void setLiquidFuel(Double liquidFuel) { this.liquidFuel = liquidFuel; }

    public Double getGasFuel() { return gasFuel; }
    public void setGasFuel(Double gasFuel) { this.gasFuel = gasFuel; }
}