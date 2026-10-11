package ru.test.weather.compare;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Сравнивает распарсенный ответ с ожидаемыми значениями.
 * <p>
 * Поля задаются путём через точку в терминах JSON API (например, {@code current.condition.text}),
 * поэтому в feature-файле видно ровно то, что приходит от сервиса.
 * Числа сравниваются как числа: ожидание {@code 6} совпадёт с фактическим {@code 6.0}.
 */
public class WeatherComparator {

    private static final String NO_VALUE = "<нет значения>";

    private final ObjectMapper mapper;

    public WeatherComparator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<FieldComparison> compare(Object actualResponse, Map<String, String> expected) {
        JsonNode actualTree = mapper.valueToTree(actualResponse);
        List<FieldComparison> result = new ArrayList<>();
        expected.forEach((field, expectedValue) -> result.add(compareField(actualTree, field, expectedValue)));
        return result;
    }

    private FieldComparison compareField(JsonNode root, String field, String expectedValue) {
        JsonNode node = resolve(root, field);
        if (node.isMissingNode() || node.isNull()) {
            return new FieldComparison(field, expectedValue, NO_VALUE, false);
        }

        String actualValue = node.asText();
        boolean matches = node.isNumber()
                ? numbersEqual(expectedValue, node.decimalValue())
                : actualValue.equals(expectedValue);

        return new FieldComparison(field, expectedValue, actualValue, matches);
    }

    private static JsonNode resolve(JsonNode root, String path) {
        JsonNode node = root;
        for (String part : path.split("\\.")) {
            node = node.path(part);
        }
        return node;
    }

    private static boolean numbersEqual(String expected, BigDecimal actual) {
        try {
            return new BigDecimal(expected.trim()).compareTo(actual) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
