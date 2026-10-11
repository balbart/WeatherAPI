package ru.test.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Тело ошибки API: {"error": {"code": 1006, "message": "No matching location found."}}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ErrorResponse(@JsonProperty("error") ApiError error) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiError(
            @JsonProperty("code") Integer code,
            @JsonProperty("message") String message
    ) {
    }
}
