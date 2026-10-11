# Weather API: автотесты на заглушках

Тесты API [weatherapi.com](https://www.weatherapi.com/docs/) (метод `GET /v1/current.json`) на Cucumber.
Сервис эмулирует WireMock, поэтому в реальный API тесты не ходят и ключ не нужен.

**Стек:** Java 19, Gradle 8.14 (wrapper), Cucumber 7 (JUnit Platform), WireMock 3, REST Assured, Jackson, AssertJ, Allure.

## Запуск

```bash
./gradlew clean test            # прогон тестов
./gradlew allureServe           # открыть отчёт Allure в браузере
./gradlew allureReport          # собрать отчёт в build/reports/allure-report/allureReport
```

JDK 19 Gradle найдёт сам, а если его нет — скачает через foojay toolchain resolver.
Allure CLI плагин тоже скачивает сам.

Если какие-то тесты упали, а отчёт нужен: `./gradlew test allureReport --continue`.

### Фильтрация по тегам

```bash
./gradlew test -Dcucumber.filter.tags="@positive"
./gradlew test -Dcucumber.filter.tags="@negative"
./gradlew test -Dcucumber.filter.tags="@mismatch"   # демонстрация расхождений (такие тесты падают нарочно)
./gradlew test -Dcucumber.filter.tags="@weather"    # всё вместе с демонстрацией
```

По умолчанию запускается `not @mismatch` (задано в `junit-platform.properties`).

## Что проверяется

### Позитивные сценарии: `features/current_weather_positive.feature`

Текущая погода в четырёх городах: Moscow, London, Tokyo, Paris. Ответ разбирается в модель
`CurrentWeatherResponse`, и каждое ожидаемое значение из таблицы примеров сравнивается с фактическим:
`location.name`, `location.country`, `current.temp_c`, `current.feelslike_c`, `current.condition.text`,
`current.wind_kph`, `current.wind_dir`, `current.pressure_mb`, `current.humidity`.

Результат сравнения по **каждому** полю попадает:
- в лог (консоль и `build/logs/tests.log`): `[OK]` или `[MISMATCH]` с ожидаемым и фактическим значением;
- в Allure: отдельный шаг на каждое поле плюс CSV-вложение со всей таблицей сравнения.

Если есть хотя бы одно расхождение, сценарий падает, и в сообщении перечислены все несовпавшие поля.
Набор примеров с тегом `@mismatch` нарочно содержит неверные ожидания, чтобы было видно, как выглядят расхождения.

Числа сравниваются как числа: ожидание `1014` совпадёт с `1014.0`.

### Негативные сценарии: `features/current_weather_negative.feature`

Ошибки из раздела API Errors документации:

| HTTP | Код  | Ситуация                 |
|------|------|--------------------------|
| 401  | 1002 | ключ не передан          |
| 400  | 1003 | параметр `q` не передан  |
| 400  | 1006 | город не найден          |
| 401  | 2006 | неверный ключ            |
| 403  | 2008 | ключ отключён            |

Проверяются HTTP-статус, `error.code` и `error.message`.

## Заглушки

`src/test/resources/wiremock/mappings` содержит маппинги, `wiremock/__files/current` — тела ответов для городов.
WireMock поднимается один раз на весь прогон на случайном порту (`Hooks` → `WeatherApiStub`).

Ошибки разведены приоритетами, как в реальном сервисе: сначала проверяется ключ, потом `q`.
Любой город, которого нет в заглушках, при валидном ключе даёт 1006.
Запросы, не попавшие ни в одну заглушку, пишутся в лог с уровнем WARN.

Ключ, который принимает заглушка, — `test-api-key` (`test.properties`).

## Структура

```
src/test/java/ru/test/weather
├── runner/CucumberTest.java        — JUnit Platform Suite для Cucumber
├── steps/                          — шаги и хуки (старт и остановка WireMock)
├── stubs/WeatherApiStub.java       — управление WireMock-сервером
├── client/WeatherApiClient.java    — REST Assured-клиент с фильтром Allure
├── model/                          — модели ответа и ошибки (records)
├── compare/                        — сравнение полей ответа с ожиданиями
└── config/TestConfig.java          — настройки из test.properties
src/test/resources
├── features/                       — сценарии на русском Gherkin
├── wiremock/                       — маппинги и тела ответов
├── junit-platform.properties       — настройки Cucumber
├── allure.properties, categories.json
└── logback-test.xml
```
