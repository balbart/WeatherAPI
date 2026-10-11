package ru.test.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ GET /v1/current.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrentWeatherResponse(
        @JsonProperty("location") Location location,
        @JsonProperty("current") Current current
) {
}
