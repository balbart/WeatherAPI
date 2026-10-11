package ru.test.weather.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Клиент API погоды. Работает с тем адресом, который ему передали, — в тестах это WireMock.
 */
public class WeatherApiClient {

    private final String baseUrl;
    private final String basePath;

    public WeatherApiClient(String baseUrl, String basePath) {
        this.baseUrl = baseUrl;
        this.basePath = basePath;
    }

    /**
     * GET /current.json. Параметр, переданный как null, в запрос не попадает
     * (так проверяются ошибки «нет ключа» и «нет q»).
     */
    public Response getCurrentWeather(String apiKey, String query) {
        RequestSpecification request = RestAssured.given()
                .baseUri(baseUrl)
                .basePath(basePath)
                .filter(new AllureRestAssured())
                .accept("application/json");

        if (apiKey != null) {
            request.queryParam("key", apiKey);
        }
        if (query != null) {
            request.queryParam("q", query);
        }

        return request.when().get("/current.json");
    }
}
