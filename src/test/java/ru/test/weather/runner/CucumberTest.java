package ru.test.weather.runner;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Точка входа JUnit Platform: запускает все feature-файлы из src/test/resources/features.
 * Настройки Cucumber (glue, плагины, теги) лежат в junit-platform.properties.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class CucumberTest {
}
