package ru.test.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Location(
        @JsonProperty("name") String name,
        @JsonProperty("region") String region,
        @JsonProperty("country") String country,
        @JsonProperty("lat") BigDecimal lat,
        @JsonProperty("lon") BigDecimal lon,
        @JsonProperty("tz_id") String tzId,
        @JsonProperty("localtime_epoch") Long localtimeEpoch,
        @JsonProperty("localtime") String localtime
) {
}
