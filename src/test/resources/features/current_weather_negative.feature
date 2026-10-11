# language: ru
@weather @negative @severity=normal @link=https://www.weatherapi.com/docs/
Функция: Ошибки API текущей погоды
  Проверяем, что сервис возвращает корректные HTTP-статусы и коды ошибок из раздела API Errors документации.
  Пустое значение ключа или города означает, что параметр не передаётся в запросе вовсе.

  Структура сценария: Ошибка <error_code> — <description>
    Допустим используется API-ключ "<key>"
    Когда пользователь запрашивает текущую погоду для "<query>"
    Тогда сервис отвечает статусом <status>
    И ответ соответствует JSON-схеме "error.json"
    И ответ содержит ошибку с кодом <error_code> и сообщением "<message>"

    Примеры:
      | description                  | key          | query          | status | error_code | message                             |
      | API-ключ не передан          |              | Moscow         | 401    | 1002       | API key is invalid or not provided. |
      | параметр q не передан        | test-api-key |                | 400    | 1003       | Parameter q is missing.             |
      | город не найден              | test-api-key | NoSuchCity1234 | 400    | 1006       | No matching location found.         |
      | API-ключ недействителен      | wrong-key    | Moscow         | 401    | 2006       | API key is invalid.                 |
      | API-ключ отключён            | disabled-key | Moscow         | 403    | 2008       | API key has been disabled.          |
