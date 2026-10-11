package ru.test.weather.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.test.weather.client.WeatherApiClient;
import ru.test.weather.compare.FieldComparison;
import ru.test.weather.compare.WeatherComparator;
import ru.test.weather.config.TestConfig;
import ru.test.weather.model.CurrentWeatherResponse;
import ru.test.weather.model.ErrorResponse;
import ru.test.weather.stubs.WeatherApiStub;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Шаги сценариев. Cucumber создаёт новый экземпляр класса на каждый сценарий,
 * поэтому поля — это состояние одного сценария.
 */
public class WeatherSteps {

    private static final Logger log = LoggerFactory.getLogger(WeatherSteps.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Адрес WireMock известен только после старта сервера в @BeforeAll, поэтому клиент создаётся при первом запросе
    private WeatherApiClient client;
    private final WeatherComparator comparator = new WeatherComparator(MAPPER);

    private String apiKey;
    private String query;
    private Response response;

    @Given("используется валидный API-ключ")
    public void useValidApiKey() {
        apiKey = TestConfig.apiKey();
    }

    /**
     * Пустая строка означает, что параметр key не передаётся вовсе.
     */
    @Given("используется API-ключ {string}")
    public void useApiKey(String key) {
        apiKey = key.isEmpty() ? null : key;
    }

    /**
     * Пустая строка означает, что параметр q не передаётся вовсе.
     */
    @When("пользователь запрашивает текущую погоду для {string}")
    public void requestCurrentWeather(String q) {
        query = q.isEmpty() ? null : q;
        log.info("Запрос текущей погоды: q={}, key={}", describe(query), describe(apiKey));
        response = client().getCurrentWeather(apiKey, query);
        log.info("Ответ: HTTP {}", response.statusCode());
    }

    @Then("сервис отвечает статусом {int}")
    public void checkStatus(int expectedStatus) {
        assertThat(response.statusCode())
                .as("HTTP-статус ответа")
                .isEqualTo(expectedStatus);
    }

    @Then("ответ соответствует JSON-схеме {string}")
    public void checkJsonSchema(String schemaFile) {
        String schemaPath = "schemas/" + schemaFile;
        Allure.addAttachment("JSON-схема: " + schemaFile, "application/json", readResource(schemaPath), ".json");
        response.then().assertThat().body(matchesJsonSchemaInClasspath(schemaPath));
        log.info("Ответ соответствует схеме {}", schemaFile);
    }

    @Then("фактическая погода совпадает с ожидаемой:")
    public void compareWithExpected(DataTable table) {
        List<FieldComparison> comparisons = compareAndReport(table);
        List<FieldComparison> mismatches = mismatchesOf(comparisons);

        if (!mismatches.isEmpty()) {
            String details = mismatches.stream().map(FieldComparison::toString)
                    .collect(Collectors.joining(System.lineSeparator() + "  - ", "  - ", ""));
            fail("Найдены расхождения для '%s' (%d из %d):%n%s"
                    .formatted(query, mismatches.size(), comparisons.size(), details));
        }
    }

    /**
     * Шаг для демонстрационного сценария: ожидания в таблице заведомо неверные,
     * расхождения так же выводятся в лог и в Allure, но сценарий проверяет, что найдены
     * именно перечисленные расхождения, и поэтому не падает.
     */
    @Then("при сравнении с ожидаемой погодой найдены расхождения в полях {string}:")
    public void compareWithExpectedMismatches(String expectedMismatchFields, DataTable table) {
        List<FieldComparison> comparisons = compareAndReport(table);

        Set<String> expectedFields = Arrays.stream(expectedMismatchFields.split(","))
                .map(String::trim)
                .filter(field -> !field.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> actualFields = mismatchesOf(comparisons).stream()
                .map(FieldComparison::field)
                .collect(Collectors.toCollection(TreeSet::new));

        assertThat(actualFields)
                .as("Поля, по которым найдены расхождения")
                .isEqualTo(expectedFields);
        log.info("Найдены ожидаемые расхождения по полям {}: сравнение работает корректно", actualFields);
    }

    /**
     * Сравнивает ответ с таблицей ожиданий и выводит результат по каждому значению:
     * в лог (OK — INFO, расхождение — WARN), отдельным шагом в Allure и CSV-вложением.
     */
    private List<FieldComparison> compareAndReport(DataTable table) {
        Map<String, String> expected = table.asMap(String.class, String.class);
        CurrentWeatherResponse actual = parse(CurrentWeatherResponse.class);

        List<FieldComparison> comparisons = comparator.compare(actual, expected);
        List<FieldComparison> mismatches = mismatchesOf(comparisons);

        log.info("Сравнение погоды для '{}': полей {}, расхождений {}", query, comparisons.size(), mismatches.size());
        for (FieldComparison c : comparisons) {
            if (c.matches()) {
                log.info("  [OK]       {}", c);
            } else {
                log.warn("  [MISMATCH] {}", c);
            }
            Allure.step(c.toString(), c.matches() ? Status.PASSED : Status.FAILED);
        }
        Allure.addAttachment("Сравнение: " + query, "text/csv", toCsv(comparisons), ".csv");
        return comparisons;
    }

    private static List<FieldComparison> mismatchesOf(List<FieldComparison> comparisons) {
        return comparisons.stream().filter(c -> !c.matches()).toList();
    }

    @Then("ответ содержит ошибку с кодом {int} и сообщением {string}")
    public void checkError(int expectedCode, String expectedMessage) {
        ErrorResponse body = parse(ErrorResponse.class);
        assertThat(body.error()).as("Объект error в теле ответа").isNotNull();

        log.info("Ошибка API: code={}, message='{}' (ожидалось code={}, message='{}')",
                body.error().code(), body.error().message(), expectedCode, expectedMessage);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(body.error().code()).as("код ошибки").isEqualTo(expectedCode);
        softly.assertThat(body.error().message()).as("сообщение об ошибке").isEqualTo(expectedMessage);
        softly.assertAll();
    }

    private WeatherApiClient client() {
        if (client == null) {
            client = new WeatherApiClient(WeatherApiStub.baseUrl(), TestConfig.basePath());
        }
        return client;
    }

    private <T> T parse(Class<T> type) {
        String body = response.asString();
        try {
            return MAPPER.readValue(body, type);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Не удалось распарсить ответ как %s: %s".formatted(type.getSimpleName(), body), e);
        }
    }

    private static String toCsv(List<FieldComparison> comparisons) {
        StringBuilder csv = new StringBuilder("Поле,Ожидалось,Получено,Результат\n");
        for (FieldComparison c : comparisons) {
            csv.append(csvCell(c.field())).append(',')
                    .append(csvCell(c.expected())).append(',')
                    .append(csvCell(c.actual())).append(',')
                    .append(c.matches() ? "OK" : "MISMATCH").append('\n');
        }
        return csv.toString();
    }

    private static String csvCell(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String readResource(String path) {
        try (InputStream in = WeatherSteps.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Не найден ресурс в classpath: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String describe(String value) {
        return value == null ? "<не передан>" : value;
    }
}
