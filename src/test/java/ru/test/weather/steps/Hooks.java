package ru.test.weather.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import ru.test.weather.stubs.WeatherApiStub;

/**
 * Поднимает WireMock один раз перед всеми сценариями и гасит после них.
 */
public class Hooks {

    @BeforeAll
    public static void startStub() {
        WeatherApiStub.start();
    }

    @AfterAll
    public static void stopStub() {
        WeatherApiStub.stop();
    }
}
