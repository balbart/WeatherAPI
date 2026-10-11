package ru.test.weather.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Настройки тестов из test.properties. Любое значение можно переопределить системным свойством (-Dkey=value).
 */
public final class TestConfig {

    private static final Properties PROPERTIES = load();

    private TestConfig() {
    }

    public static String apiKey() {
        return get("weather.api.key");
    }

    public static String basePath() {
        return get("weather.api.basePath");
    }

    private static String get(String key) {
        String value = System.getProperty(key, PROPERTIES.getProperty(key));
        if (value == null) {
            throw new IllegalStateException("Не задан параметр конфигурации: " + key);
        }
        return value;
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = TestConfig.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (in == null) {
                throw new IllegalStateException("Не найден test.properties в classpath");
            }
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
