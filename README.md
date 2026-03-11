# DrawMap

Мобильное приложение для записи и просмотра маршрутов на карте с хранением данных на бэкенде.

---

## О проекте

DrawMap позволяет пользователю:
- просматривать интерактивную карту (OpenStreetMap) на Android-устройстве;
- начинать запись GPS-маршрута;
- прикреплять фотографии с геолокацией к маршруту;
- просматривать сохранённые маршруты в галерее.

Проект состоит из двух частей:

| Часть | Каталог | Описание |
|-------|---------|----------|
| **Backend** | `Backend/` | REST API на ASP.NET Core 9 + PostgreSQL |
| **Mobile** | `Mobile/` | Android-приложение на Kotlin + Jetpack Compose |

---

## Стек технологий

### Backend
| Компонент | Технология |
|-----------|-----------|
| Язык | C# 13 / .NET 9 |
| Фреймворк | ASP.NET Core Web API |
| База данных | PostgreSQL 17 (через Entity Framework Core + Npgsql) |
| Документация API | Swagger / Swashbuckle |
| Reverse-proxy | YARP (шлюз `DrawMap.Gateway`) |
| Контейнеризация | Docker / Docker Compose |
| Миграции | EF Core Migrations |

### Mobile
| Компонент | Технология |
|-----------|-----------|
| Язык | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Карта | OSMDroid (OpenStreetMap) |
| Архитектура | MVVM (ViewModel + ViewModelScope) |
| Навигация | Activity-based + кастомный `Navigator` |
| Сборка | Gradle (Kotlin DSL) |

---

## Структура репозитория

```
DrawMap/
├── Backend/
│   ├── docker-compose.yml          # Запуск всей инфраструктуры
│   ├── Dockerfile                  # Образ Web API
│   ├── DrawMap.Domain/             # Доменные модели (Route, Photo, Location)
│   ├── DrawMap.DataSources/        # EF Core DbContext + миграции
│   ├── DrawMap.Repositories/       # Репозитории (RouteRepository, PhotoRepository)
│   ├── DrawMap.WebAPI/             # REST API + сервисы
│   │   ├── Controllers/RouteController.cs
│   │   └── Services/RouteService.cs
│   └── DrawMap.Gateway/            # YARP reverse-proxy
└── Mobile/
    └── app/src/main/java/com/example/drawmap/
        ├── ui/splash/              # Экран загрузки
        ├── ui/home/                # Главный экран с картой
        ├── ui/gallery/             # Галерея маршрутов
        └── ui/navigation/          # Navigator
```

---

## Как запустить

### Backend (через Docker Compose)

> Требования: **Docker Desktop** (или Docker Engine + Compose plugin)

```bash
cd Backend
docker compose up --build
```

После старта:
- API доступен через шлюз: `http://localhost:5000`
- Swagger UI: `http://localhost:5000/swagger`
- PostgreSQL порт: `5432` (пользователь `postgres`, пароль `postgres`, БД `drawmap`)

Миграции применяются **автоматически** при первом запуске.

### Backend (локально без Docker)

> Требования: **.NET 9 SDK**, запущенный PostgreSQL

1. Убедитесь, что PostgreSQL доступен на `localhost:5432` с параметрами из `appsettings.json`.
2. Примените миграции вручную (если нужно):
   ```bash
   cd Backend
   dotnet ef database update --project DrawMap.DataSources --startup-project DrawMap.WebAPI
   ```
3. Запустите API:
   ```bash
   dotnet run --project DrawMap.WebAPI
   ```

### Mobile

> Требования: **Android Studio Hedgehog+**, JDK 11+, эмулятор или устройство Android API 24+

1. Откройте папку `Mobile/` в Android Studio.
2. Дождитесь синхронизации Gradle.
3. Нажмите **Run ▶** или `Shift+F10`.

---

## Моки (заглушки)

Мобильное приложение содержит явные заглушки для функциональности, которая будет реализована позже:

| Файл | Место | Что заглушено |
|------|-------|---------------|
| `HomeActivity.kt` | `setupButtons()` → `fabStartRecording` | Запись маршрута показывает Toast «▶ Запись маршрута (заглушка)» |
| `HomeActivity.kt` | `setupNavigation()` → `nav_heatmap` | Тепловая карта показывает Toast «🔥 Heatmap (заглушка)» |
| `HomeActivity.kt` | `setupMap()` | Геолокация пользователя захардкожена на Москву (`55.7558, 37.6173`) |
| `HomeViewModel.kt` | `onStartRecordingClick()` | GPS-трекинг помечен `// TODO: Запуск GPS-трекинга` |
| `SplashViewModel.kt` | `loadUserData()` | Загрузка данных помечена `// TODO` (авторизация, Room-кэш) |
| `GalleryActivity.kt` | `btnViewRoute` | Просмотр маршрута показывает Toast «Просмотр маршрута (заглушка)» |

---

## Реализованный сценарий

### Сценарий: управление маршрутом (CRUD)

Сценарий реализован на бэкенде и доступен через REST API.

#### Шаги и проверка через Swagger UI (`http://localhost:5000/swagger`)

**1. Создать маршрут**

`POST /api/Route`

```json
{
  "locations": [
    { "longitude": 37.6173, "latitude": 55.7558 },
    { "longitude": 37.6200, "latitude": 55.7600 }
  ],
  "photos": []
}
```

Ответ `201 Created` содержит объект маршрута с присвоенным `id`.

---

**2. Получить маршрут**

`GET /api/Route/{routeId}`

Подставьте `id` из предыдущего ответа. Должен вернуться маршрут с локациями.

---

**3. Добавить фото к маршруту**

`POST /api/Route/{routeId}/photos`

```json
{
  "location": { "longitude": 37.6185, "latitude": 55.7575 },
  "data": null
}
```

Ответ `201 Created` содержит объект фото с `id` и `routeId`.

---

**4. Обновить маршрут**

`PUT /api/Route/{routeId}`

```json
{
  "locations": [
    { "longitude": 37.6100, "latitude": 55.7500 }
  ],
  "photos": []
}
```

Ответ `200 OK` с `true`.

---

**5. Удалить маршрут**

`DELETE /api/Route/{routeId}`

Ответ `200 OK` с `true`. Все связанные фотографии удаляются каскадно.

---

#### Проверка через curl

```bash
# Создать маршрут
curl -X POST http://localhost:5000/api/Route \
  -H "Content-Type: application/json" \
  -d '{"locations":[{"longitude":37.6173,"latitude":55.7558}],"photos":[]}'

# Получить маршрут (подставьте <id>)
curl http://localhost:5000/api/Route/<id>

# Удалить маршрут
curl -X DELETE http://localhost:5000/api/Route/<id>
```
