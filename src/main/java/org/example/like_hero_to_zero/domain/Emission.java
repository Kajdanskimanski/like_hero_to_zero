package org.example.like_hero_to_zero.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "status")
    private String status = "APPROVED";

    // NEU: Wer hat die Daten erstellt?
    @Column(name = "created_by")
    private String createdBy; // Username des Users oder "system" für API-Daten

    @Column(name = "is_editable")
    private Boolean isEditable = true; // false für API-Daten

    @Column(name = "data_type")
    private String dataType; // "TOTAL", "PER_CAPITA", "SOLID_FUEL", "LIQUID_FUEL", "GAS_FUEL"

    @Column(name = "unit")
    private String unit; // "kt", "t per capita", "MtCO2"

    public Emission() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Emission(String country, Integer year, Double co2Emissions, String source) {
        this();
        this.country = country;
        this.year = year;
        this.co2Emissions = co2Emissions;
        this.source = source;
        this.createdBy = "system"; // API-Daten
        this.isEditable = false;
    }

    // Getters und Setters
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
}