package ru.test.weather.stubs;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * WireMock-заглушка API weatherapi.com.
 * <p>
 * Маппинги лежат в src/test/resources/wiremock/mappings, тела ответов — в wiremock/__files.
 * Сервер поднимается один раз на весь прогон на случайном свободном порту.
 */
public final class WeatherApiStub {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiStub.class);

    private static WireMockServer server;

    private WeatherApiStub() {
    }

    public static synchronized void start() {
        if (server != null && server.isRunning()) {
            return;
        }
        server = new WireMockServer(wireMockConfig()
                .dynamicPort()
                .usingFilesUnderClasspath("wiremock"));
        server.start();

        // Любой запрос, не попавший ни в одну заглушку, — повод разобраться, поэтому пишем его в лог
        server.addMockServiceRequestListener((request, response) -> {
            if (!request.getUrl().startsWith("/__admin") && response.getStatus() == 404) {
                log.warn("Запрос не совпал ни с одной заглушкой: {} {}", request.getMethod(), request.getUrl());
            }
        });

        log.info("WireMock запущен: {} (загружено заглушек: {})", baseUrl(), server.getStubMappings().size());
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop();
            log.info("WireMock остановлен");
            server = null;
        }
    }

    public static String baseUrl() {
        if (server == null) {
            throw new IllegalStateException("WireMock не запущен");
        }
        return server.baseUrl();
    }
}
