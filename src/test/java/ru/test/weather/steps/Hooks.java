package ru.test.weather.steps;

import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.test.weather.config.TestConfig;
import ru.test.weather.stubs.WeatherApiStub;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Поднимает WireMock один раз перед всеми сценариями и гасит после них.
 * Заодно пишет environment.properties — блок Environment на главной странице отчёта Allure.
 */
public class Hooks {

    private static final Logger log = LoggerFactory.getLogger(Hooks.class);

    @BeforeAll
    public static void startStub() {
        WeatherApiStub.start();
        writeAllureEnvironment();
    }

    @AfterAll
    public static void stopStub() {
        WeatherApiStub.stop();
    }

    private static void writeAllureEnvironment() {
        Map<String, String> env = new LinkedHashMap<>();
        env.put("Service", "weatherapi.com (WireMock stub)");
        env.put("Base.URL", WeatherApiStub.baseUrl() + TestConfig.basePath());
        env.put("Java", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
        env.put("OS", System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        env.put("Cucumber", System.getProperty("versions.cucumber", "n/a"));
        env.put("WireMock", System.getProperty("versions.wiremock", "n/a"));
        env.put("REST.Assured", System.getProperty("versions.restassured", "n/a"));
        env.put("Allure.Java", System.getProperty("versions.allure", "n/a"));
        env.put("Cucumber.Tags", System.getProperty("cucumber.filter.tags", "default (junit-platform.properties)"));

        Path dir = Path.of(System.getProperty("allure.results.directory", "build/allure-results"));
        try {
            Files.createDirectories(dir);
            // Только ASCII: Allure читает файл как обычный .properties (ISO-8859-1)
            try (Writer writer = Files.newBufferedWriter(dir.resolve("environment.properties"), StandardCharsets.UTF_8)) {
                for (Map.Entry<String, String> entry : env.entrySet()) {
                    writer.write(entry.getKey() + "=" + entry.getValue() + System.lineSeparator());
                }
            }
        } catch (IOException e) {
            // Отчёт без блока Environment — не повод ронять прогон
            log.warn("Не удалось записать environment.properties для Allure: {}", e.getMessage());
        }
    }
}
