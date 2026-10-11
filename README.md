# Weather API: автотесты на заглушках

> **Задание выполнено с использованием ИИ.** Проект сделан в паре с ИИ-ассистентом Claude (Anthropic):
> я ставил задачу, принимал решения по подходу, запускал тесты у себя и разбирал с ассистентом ошибки.
> Вся переписка, включая найденные проблемы и то, как они решались, лежит в файле [AI_SESSION.md](AI_SESSION.md).

Тесты API [weatherapi.com](https://www.weatherapi.com/docs/) (метод `GET /v1/current.json`) на Cucumber.
Сервис эмулирует WireMock, поэтому в реальный API тесты не ходят и ключ не нужен.

**Стек:** Java 19, Gradle 8.14 (wrapper), Cucumber 7 (JUnit Platform), WireMock 3, REST Assured + json-schema-validator, Jackson, AssertJ, Allure.

## Запуск

Прогон тестов:
```bash
./gradlew clean test
```

Открыть отчёт Allure в браузере (поднимает локальный сервер):
```bash
./gradlew allureServe
```

Собрать отчёт в `build/reports/allure-report/allureReport`:
```bash
./gradlew allureReport
```
Готовый отчёт нужно открывать через сервер (`allure open build/reports/allure-report/allureReport`),
а не двойным кликом по `index.html`: при открытии с диска браузер не даст загрузить данные.

JDK 19 Gradle найдёт сам, а если его нет — скачает через foojay toolchain resolver.
Allure CLI плагин тоже скачивает сам.

Если какие-то тесты упали, а отчёт нужен: `./gradlew test allureReport --continue`.

### Фильтрация по тегам

Только позитивные или только негативные сценарии:
```bash
./gradlew test -Dcucumber.filter.tags="@positive"
./gradlew test -Dcucumber.filter.tags="@negative"
```

Только демонстрация вывода расхождений:
```bash
./gradlew test -Dcucumber.filter.tags="@demo"
```

Всё, кроме демонстрации:
```bash
./gradlew test -Dcucumber.filter.tags="not @demo"
```

По умолчанию запускаются все сценарии.

## Что проверяется

### Позитивные сценарии: `features/current_weather_positive.feature`

Текущая погода в четырёх городах: Moscow, London, Tokyo, Paris. Ответ сначала проверяется
по JSON-схеме `schemas/current-weather.json`, затем разбирается в модель
`CurrentWeatherResponse`, и каждое ожидаемое значение из таблицы примеров сравнивается с фактическим:
`location.name`, `location.country`, `current.temp_c`, `current.feelslike_c`, `current.condition.text`,
`current.wind_kph`, `current.wind_dir`, `current.pressure_mb`, `current.humidity`.

Результат сравнения по **каждому** полю попадает:
- в лог (консоль и `build/logs/tests.log`): `[OK]` или `[MISMATCH]` с ожидаемым и фактическим значением;
- в Allure: отдельный шаг на каждое поле плюс CSV-вложение со всей таблицей сравнения.

Если есть хотя бы одно расхождение, сценарий падает, и в сообщении перечислены все несовпавшие поля.

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

Проверяются HTTP-статус, соответствие тела схеме `schemas/error.json`, `error.code` и `error.message`.

### Демонстрация расхождений: `features/current_weather_mismatch_demo.feature`

Отдельный сценарий **создан специально, чтобы показать, как расхождения выводятся в лог**.
В нём ожидания для Moscow нарочно неверны по трём полям: `current.temp_c`, `current.condition.text`, `current.humidity`.

Что видно после прогона:
- в логе — три строки `WARN ... [MISMATCH] поле: ожидалось '...', получено '...'` среди строк `[OK]`;
- в Allure — красные подшаги по каждому несовпавшему полю и CSV-таблица сравнения.

Сам сценарий проверяет, что найдены ровно эти три расхождения, поэтому он зелёный и не ломает сборку.

### JSON-схемы: `src/test/resources/schemas`

Схемы (draft-04, их поддерживает валидатор REST Assured) описывают контракт по документации:
обязательные поля, типы, допустимые диапазоны (`humidity` 0–100, `is_day` 0/1, `wind_degree` 0–360),
формат `localtime`. Дополнительные поля в ответе погоды разрешены, потому что API может их добавлять.
Схема ошибки строгая: только `error.code` и `error.message`.
Схема прикладывается к шагу в Allure.

## Отчёт Allure

Кроме шагов, HTTP-запросов и ответов в отчёте есть:
- **Environment** — адрес заглушки, версии Java, Cucumber, WireMock, REST Assured и Allure, ОС, фильтр тегов
  (`environment.properties` пишется в `Hooks` перед прогоном);
- **Severity** — `critical` для позитивных сценариев, `normal` для негативных, `minor` для демонстрации (тег `@severity=...`);
- **Links** — ссылка на документацию API (тег `@link=...`);
- **Behaviors** — группировка Epic → Feature → Story (epic задан в `allure.properties`);
- **Categories** — упавшие тесты разложены по причинам: расхождения, нарушение схемы, неожиданный ответ API, сломанные тесты.

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
├── schemas/                        — JSON-схемы ответа погоды и ошибки
├── wiremock/                       — маппинги и тела ответов
├── junit-platform.properties       — настройки Cucumber
├── allure.properties, categories.json
└── logback-test.xml
```
