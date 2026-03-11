# DrawMap

Мобильное приложение для записи и просмотра GPS-маршрутов с поддержкой фотографий. Приложение позволяет отображать карту (OSMDroid), записывать маршрут движения, прикреплять фото к точкам маршрута и просматривать сохранённые маршруты в галерее.

---

## Стек технологий

### 📱 Mobile (Android)

| Технология | Версия |
|---|---|
| Kotlin | 2.0.0 |
| Android Gradle Plugin | 9.1.0 |
| Jetpack Compose + Material 3 | BOM 2024.06.00 |
| Navigation Compose | 2.8.0 |
| ViewModel + Lifecycle | 2.8.4 |
| OSMDroid (карты) | 6.1.18 |
| Google Play Services Location | 21.3.0 |
| Retrofit 2 + OkHttp | 2.11.0 / 4.12.0 |
| Room | 2.6.1 |
| Kotlin Coroutines | 1.8.1 |

### 🖥️ Backend (.NET)

| Технология | Версия |
|---|---|
| .NET / ASP.NET Core Web API | 9.0 |
| Entity Framework Core + Npgsql | — |
| PostgreSQL | 17 (Alpine) |
| Swagger / OpenAPI | — |
| Docker + Docker Compose | — |

### Архитектура Backend

Решение разбито на проекты по слоям:

```
DrawMap.Domain        — доменные модели (Route, Photo, Location)
DrawMap.DataSources   — DbContext, миграции EF Core
DrawMap.Repositories  — интерфейсы и реализации репозиториев
DrawMap.WebAPI        — Controllers, Services, точка входа
```

---

## Структура репозитория

```
DrawMap/
├── Backend/          # ASP.NET Core Web API
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── DrawMap.sln
│   ├── DrawMap.Domain/
│   ├── DrawMap.DataSources/
│   ├── DrawMap.Repositories/
│   └── DrawMap.WebAPI/
└── Mobile/           # Android-приложение
    └── app/src/main/java/com/example/drawmap/
        ├── data/
        │   ├── model/        — модели данных (Route, GalleryItem)
        │   └── repository/   — интерфейсы и реализации репозиториев (в т.ч. моки)
        ├── di/               — ServiceLocator
        └── ui/               — экраны и ViewModel-ы
```

---

## Где находятся моки

Моки расположены в пакете `com.example.drawmap.data.repository`:

| Файл | Описание |
|---|---|
| `MockRouteRepository.kt` | Возвращает 3 захардкоженных маршрута (*Morning Walk*, *Park Loop*, *Evening Stroll*) с точками на карте Москвы, расстоянием и расчётным временем |
| `MockGalleryRepository.kt` | Возвращает список из 3 элементов галереи, соответствующих тем же маршрутам |

Моки подключаются через `ServiceLocator` (`di/ServiceLocator.kt`):

```kotlin
object ServiceLocator {
    val galleryRepository: GalleryRepository by lazy { MockGalleryRepository() }
    val routeRepository: RouteRepository   by lazy { MockRouteRepository() }
}
```

Чтобы переключиться на реальные репозитории, достаточно заменить реализации в `ServiceLocator`.

---

## Как запустить приложение

### Backend

#### Вариант 1 — Docker Compose (рекомендуется)

```bash
cd Backend
docker compose up --build
```

- API будет доступен на `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger`
- PostgreSQL: `localhost:5432` (БД `drawmap`, пользователь `postgres`, пароль `postgres`)

Миграции применяются автоматически при старте контейнера.

#### Вариант 2 — локальный запуск

1. Запустите PostgreSQL и создайте базу данных `drawmap`.
2. Проверьте строку подключения в `DrawMap.WebAPI/appsettings.json`:
   ```json
   "DefaultConnection": "Host=localhost;Port=5432;Database=drawmap;Username=postgres;Password=postgres"
   ```
3. Запустите API:
   ```bash
   cd Backend
   dotnet run --project DrawMap.WebAPI
   ```

---

### Mobile

1. Откройте папку `Mobile/` в **Android Studio** (Meerkat | 2024.3+).
2. Дождитесь синхронизации Gradle.
3. Запустите приложение на эмуляторе или физическом устройстве (**Run ▶**).

> **Примечание:** Приложение работает полностью на моках — Backend не требуется для проверки мобильного сценария.

---

## Реализованный сценарий и как его проверить

### Сценарий: просмотр сохранённых маршрутов

**Шаги:**

1. **Запустите приложение** на Android-устройстве/эмуляторе.
2. **Главный экран (HomeActivity)** — открывается карта (OSMDroid). При наличии интернета тайлы загружаются, при отсутствии отображается оффлайн-заглушка.
3. Нажмите на **иконку галереи** в нижней навигационной панели (`BottomNavigationView`) — откроется `GalleryActivity`.
4. В галерее отображается список маршрутов, загруженных из `MockGalleryRepository`:
   - *Morning Walk*
   - *Park Loop*
   - *Evening Stroll*
5. Нажмите на любой маршрут — откроется **`RouteDetailActivity`** с картой и нанесённым треком маршрута, показателями дистанции и времени.
6. При наличии разрешения на геолокацию кнопка **«Моё местоположение»** (FAB) центрирует карту на текущей позиции пользователя.

### Сценарий: REST API маршрутов (Backend)

После запуска Backend через Docker Compose:

| Метод | URL | Описание |
|---|---|---|
| `POST` | `/api/route` | Создать маршрут |
| `GET` | `/api/route/{routeId}` | Получить маршрут по ID |
| `PUT` | `/api/route/{routeId}` | Обновить маршрут |
| `DELETE` | `/api/route/{routeId}` | Удалить маршрут |
| `POST` | `/api/route/{routeId}/photos` | Добавить фото к маршруту |

**Пример создания маршрута через Swagger (`http://localhost:8080/swagger`):**

```json
POST /api/route
{
  "locations": [
    { "latitude": 55.7558, "longitude": 37.6173 },
    { "latitude": 55.7565, "longitude": 37.6190 }
  ],
  "photos": []
}
```

Ответ вернёт созданный объект с присвоенным `id` (GUID).
