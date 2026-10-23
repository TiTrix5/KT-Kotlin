# Ktor To-Do API

Учебный REST API на Kotlin и Ktor для управления задачами. Проект использует JSON-сериализацию через `ContentNegotiation` и `kotlinx.serialization`.

## Требования

- JDK 17 или новее
- Gradle (либо Gradle Wrapper, если он создан в вашей среде)

## Запуск

1. Откройте проект в IntelliJ IDEA.
2. Убедитесь, что выбран JDK 17+.
3. Запустите задачу Gradle `run` или выполните в терминале:

   ```bash
   gradle run
   ```

4. Сервер будет доступен по адресу `http://localhost:8080`.

Если в проекте есть Gradle Wrapper, используйте `./gradlew run` (Linux/macOS) или `gradlew.bat run` (Windows).

## Эндпоинты

| Метод | URL | Описание |
|---|---|---|
| GET | `/` | Информация об API |
| GET | `/tasks` | Получить все задачи |
| GET | `/tasks?completed=true` | Фильтр по статусу выполнения |
| GET | `/tasks?search=ktor` | Поиск по названию и описанию |
| GET | `/tasks/{id}` | Получить задачу по ID |
| POST | `/tasks` | Создать задачу |
| DELETE | `/tasks/{id}` | Удалить задачу по ID |

### Создать задачу

```bash
curl -i -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Изучить Ktor","description":"Сделать проект","completed":false}'
```

Ожидаемый статус: `201 Created`.

### Получить все задачи

```bash
curl -i http://localhost:8080/tasks
```

Ожидаемый статус: `200 OK`.

### Получить задачу по ID

```bash
curl -i http://localhost:8080/tasks/1
```

Ожидаемый статус: `200 OK`; если задачи нет — `404 Not Found`.

### Query-параметры

```bash
curl -i "http://localhost:8080/tasks?completed=false"
curl -i "http://localhost:8080/tasks?search=ktor"
```

Параметр `completed` принимает только `true` или `false`. При другом значении возвращается `400 Bad Request`.

### Удалить задачу

```bash
curl -i -X DELETE http://localhost:8080/tasks/1
```

Ожидаемый статус: `200 OK`; если задачи нет — `404 Not Found`.

## HTTP-коды

- `200 OK` — успешное чтение или удаление.
- `201 Created` — задача создана.
- `400 Bad Request` — неверный ID, query-параметр или JSON/данные запроса.
- `404 Not Found` — задача не найдена.
- `500 Internal Server Error` — непредвиденная ошибка сервера.

## Запуск тестов

```bash
gradle test
```

## Важное замечание

Данные хранятся в памяти процесса и очищаются при перезапуске сервера. Для учебного задания это позволяет обойтись без настройки базы данных.

## Загрузка в GitHub

Создайте пустой репозиторий на GitHub, затем из корневой папки проекта выполните:

```bash
git init
git add .
git commit -m "Implement Ktor To-Do API"
git branch -M main
git remote add origin https://github.com/ВАШ_ЛОГИН/ИМЯ_РЕПОЗИТОРИЯ.git
git push -u origin main
```

Замените URL на адрес вашего репозитория.
