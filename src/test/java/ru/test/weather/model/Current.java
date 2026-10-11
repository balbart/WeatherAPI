package ru.test.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Current(
        @JsonProperty("last_updated_epoch") Long lastUpdatedEpoch,
        @JsonProperty("last_updated") String lastUpdated,
        @JsonProperty("temp_c") BigDecimal tempC,
        @JsonProperty("temp_f") BigDecimal tempF,
        @JsonProperty("is_day") Integer isDay,
        @JsonProperty("condition") Condition condition,
        @JsonProperty("wind_mph") BigDecimal windMph,
        @JsonProperty("wind_kph") BigDecimal windKph,
        @JsonProperty("wind_degree") Integer windDegree,
        @JsonProperty("wind_dir") String windDir,
        @JsonProperty("pressure_mb") BigDecimal pressureMb,
        @JsonProperty("pressure_in") BigDecimal pressureIn,
        @JsonProperty("precip_mm") BigDecimal precipMm,
        @JsonProperty("precip_in") BigDecimal precipIn,
        @JsonProperty("humidity") Integer humidity,
        @JsonProperty("cloud") Integer cloud,
        @JsonProperty("feelslike_c") BigDecimal feelslikeC,
        @JsonProperty("feelslike_f") BigDecimal feelslikeF,
        @JsonProperty("vis_km") BigDecimal visKm,
        @JsonProperty("vis_miles") BigDecimal visMiles,
        @JsonProperty("uv") BigDecimal uv,
        @JsonProperty("gust_mph") BigDecimal gustMph,
        @JsonProperty("gust_kph") BigDecimal gustKph
) {
}
