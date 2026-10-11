package ru.test.weather.compare;

/**
 * Результат сравнения одного поля ответа с ожидаемым значением.
 */
public record FieldComparison(String field, String expected, String actual, boolean matches) {

    @Override
    public String toString() {
        return matches
                ? "%s: OK (%s)".formatted(field, actual)
                : "%s: ожидалось '%s', получено '%s'".formatted(field, expected, actual);
    }
}
