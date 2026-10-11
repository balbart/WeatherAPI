package ru.test.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Condition(
        @JsonProperty("text") String text,
        @JsonProperty("icon") String icon,
        @JsonProperty("code") Integer code
) {
}
