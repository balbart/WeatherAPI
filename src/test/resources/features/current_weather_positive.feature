# language: ru
@weather @positive @severity=critical @link=https://www.weatherapi.com/docs/
Функция: Текущая погода по городам
  Как потребитель API weatherapi.com
  Я хочу получать текущую погоду по названию города
  Чтобы показывать пользователю актуальные данные

  Ответы сервиса эмулирует WireMock: заглушки лежат в src/test/resources/wiremock.
  Ответ проверяется по JSON-схеме, затем каждое ожидаемое значение сравнивается с фактическим.
  Результат по каждому полю пишется в лог и в отчёт Allure.

  Предыстория:
    Допустим используется валидный API-ключ

  Структура сценария: Текущая погода в городе <query>
    Когда пользователь запрашивает текущую погоду для "<query>"
    Тогда сервис отвечает статусом 200
    И ответ соответствует JSON-схеме "current-weather.json"
    И фактическая погода совпадает с ожидаемой:
      | location.name          | <name>        |
      | location.country       | <country>     |
      | current.temp_c         | <temp_c>      |
      | current.feelslike_c    | <feelslike_c> |
      | current.condition.text | <condition>   |
      | current.wind_kph       | <wind_kph>    |
      | current.wind_dir       | <wind_dir>    |
      | current.pressure_mb    | <pressure_mb> |
      | current.humidity       | <humidity>    |

    Примеры: Ожидания совпадают с ответом сервиса
      | query  | name   | country        | temp_c | feelslike_c | condition     | wind_kph | wind_dir | pressure_mb | humidity |
      | Moscow | Moscow | Russia         | 6.2    | 3.4         | Overcast      | 13.3     | WSW      | 1014.0      | 81       |
      | London | London | United Kingdom | 13.0   | 11.2        | Light rain    | 19.1     | SW       | 1008.0      | 82       |
      | Tokyo  | Tokyo  | Japan          | 18.4   | 18.4        | Clear         | 6.5      | N        | 1019.0      | 68       |
      | Paris  | Paris  | France         | 15.1   | 14.6        | Partly cloudy | 10.8     | SSW      | 1012.0      | 72       |
