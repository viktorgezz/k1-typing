# K1 Typing

Веб-приложение для обучения печати: одиночные упражнения и мультиплеерные соревнования в реальном времени.

---

## Содержание

- [Технологический стек](#технологический-стек)
- [Архитектура проекта](#архитектура-проекта)
- [Архитектура базы данных](#архитектура-базы-данных)
- [Архитектура Redis](#архитектура-redis)
- [Структура бэкенда](#структура-бэкенда)
- [Структура фронтенда](#структура-фронтенда)
- [Запуск проекта](#запуск-проекта)
- [Переменные окружения](#переменные-окружения)
- [CI/CD и деплой](#cicd-и-деплой)

---

## Технологический стек

| Слой | Технологии |
|------|------------|
| **Backend** | Java 21, Spring Boot 3.5, Spring Security, JWT, Spring Data JPA, Spring Data Redis, Spring WebSocket (STOMP) |
| **Frontend** | Vue 3, Pinia, Vue Router, Vite, Tailwind CSS, Axios, STOMP.js, SockJS |
| **БД** | PostgreSQL 18 |
| **Кэш / сессии мультиплеера** | Redis 7 |
| **Инфраструктура** | Docker, Docker Compose |
| **Мониторинг** | JavaMelody |

---

## Архитектура проекта

```
┌─────────────────┐     HTTP / WebSocket      ┌─────────────────────────┐
│  k1-typing-     │ ◄──────────────────────►  │  k1-typing-backend      │
│  frontend       │   REST API, STOMP /ws     │  (Spring Boot)          │
│  (Vue 3, Vite)  │                           │                         │
└─────────────────┘                           │  ┌─────────┐ ┌───────┐  │
                                              │  │ Redis   │ │ Post- │  │
                                              │  │ (multi- │ │ gres  │  │
                                              │  │ player) │ │ (JPA) │  │
                                              │  └─────────┘ └───────┘  │
                                              └─────────────────────────┘
```

- **Frontend**: SPA на Vue 3, общение с бэкендом через REST и WebSocket (STOMP over SockJS).
- **Backend**: монолит на Spring Boot; авторизация — JWT (access + refresh), мультиплеер — Redis + WebSocket.

---

## Архитектура базы данных

[Архитектура БД](./docs/typing_bd.jpg)

**Описание основных сущностей PostgreSQL:**

| Таблица | Назначение |
|--------|------------|
| `users` | Пользователи, роли, учётные данные |
| `exercises` | Упражнения (текст, язык), владелец — `users` |
| `contests` | Соревнования: статус (`CREATED` → `WAITING` → `PROGRESS` → `FINISHED`), лимит участников, связь с `exercises` |
| `participants` | Участники контеста (`contest` + `user`), уникальная пара |
| `result_items` | Результаты: скорость, точность, место, время; привязка к `contest` и `user` |
| `refresh_tokens` | Refresh-токены JWT, привязка к `users` |

Связи: `User` ↔ `Exercise` (один ко многим), `Exercise` ↔ `Contest` (один ко многим), `Contest` ↔ `Participants`, `Contest` ↔ `ResultItem`; у каждой связи — своя сторона «многие» к `User`.

---

## Архитектура Redis

Redis используется **только для мультиплеерных комнат**: состояние комнаты, участники, прогресс печати, готовность, порядок финиша. Постоянные данные (пользователи, упражнения, контесты, результаты) хранятся в PostgreSQL.

### Роль Redis в проекте

- Хранение **временного** состояния активных комнат (пока идёт набор участников или соревнование).
- Быстрый доступ к прогрессу, готовности и финишу при обработке WebSocket-сообщений.
- Автоматическое истечение данных по TTL (30 минут), чтобы не засорять хранилище.

### Подключение и конфигурация

- **Клиент**: Lettuce (Spring Data Redis), standalone.
- **Пул**: `GenericObjectPoolConfig` — `maxTotal=32`, `maxIdle=16`, `minIdle=4`, `maxWait=500ms`.
- **Таймауты**: `commandTimeout=2000ms`, `shutdownTimeout=100ms`.
- **Протокол**: RESP2.
- **Сериализация**:
  - Ключи: `StringRedisSerializer`.
  - Значения и поля хэшей: `GenericJackson2JsonRedisSerializer`.

Параметры подключения задаются в `application.yaml` и переменных окружения:

```yaml
spring.data.redis:
  host: ${REDIS_HOST:localhost}
  port: ${REDIS_PORT:6379}
  password: ${REDIS_PASSWORD:...}
  database: 0
```

В Docker Compose Redis поднимается как отдельный сервис с AOF (`--appendonly yes`) и паролем.

### Схема ключей

Все ключи строятся по шаблону `contest:{idContest}:...` через `RedisKeyGenerator`. TTL ключей комнаты — **30 минут** (`RedisKeyGenerator.TTL_ROOM`).

| Ключ | Тип | Описание |
|------|-----|----------|
| `contest:{id}:info` | Hash | Метаданные комнаты: `idExercise`, `participantsMax` |
| `contest:{id}:participants` | Set | ID участников (строки) |
| `contest:{id}:usernames` | Hash | `idUser → username` |
| `contest:{id}:progress` | Hash | `idUser → JSON` с `{ progressPercent, speed, accuracy }` |
| `contest:{id}:ready` | Set | ID пользователей, нажавших «Готов» |
| `contest:{id}:finishers` | Sorted Set | `idUser → timestamp` финиша (для определения мест 1–2–3) |

### Сервисы и операции

| Сервис | Назначение | Основные операции |
|--------|------------|-------------------|
| **RoomService** | Жизненный цикл комнаты | `createRoom` (info + TTL), `deleteRoom` (удаление всех ключей комнаты), `roomExists`, `getParticipantsMax` |
| **ParticipantsService** | Участники | `addParticipant` (participants, usernames, progress), `removeParticipant`, `getParticipantIds`, `getParticipantNames`, `getParticipantsCount`, `isRoomFull`, `isParticipant` |
| **ProgressService** | Прогресс печати | `updateProgress`, `getProgressAll`, `getProgressByUser` (хранение в `contest:{id}:progress`) |
| **ReadyService** | Готовность к старту | `markReady`, `unmarkReady`, `isReady`, `getReadyCount`, `getReadyParticipantIds`, `clearReady` (при старте забега) |
| **FinishService** | Финиш и места | `registerFinish` (ZADD в finishers), `getFinishersCount`, `isContestComplete` |

- При **создании комнаты**: создаётся `contest:{id}:info`, в него пишутся `idExercise` и `participantsMax`, выставляется TTL.
- При **присоединении**: добавляются запись в `participants`, `usernames`, начальный прогресс в `progress`; при **выходе** — удаление из этих структур.
- **Прогресс** обновляется по WebSocket и сохраняется в `progress`; раздаётся подписчикам топика.
- **Готовность**: по нажатию «Готов» — добавление в `ready`; при старте — `clearReady`.
- **Финиш**: запись в `finishers` с временной меткой, определение места (1–2–3 или без места). При финише всех — контест завершается, лидерборд строится по PostgreSQL.

### Связь с WebSocket

- Обработчики STOMP (`ContestWebSocketHandler`): `progress`, `finish`, `ready`.
- Они вызывают соответствующие Redis-сервисы и рассылают обновления по топикам `/topic/contest/{id}/...` (progress, player-joined, player-left, player-ready, player-finished, countdown, start, finished).

---

## Структура бэкенда

```
k1-typing-backend/
├── config/
│   └── RedisConfig.java          # Lettuce, пул, RedisTemplate
├── auth/                         # Регистрация, логин, JWT, обновление данных пользователя
├── domain/
│   ├── user/                     # User, Role
│   ├── exercises/                # Exercise, Language
│   ├── contest/                  # Contest, Status
│   ├── participant/              # Participants
│   ├── result_item/              # ResultItem, Place
│   └── multiplayer/
│       ├── redis/                # Ключи, сервисы Redis (Room, Participants, Progress, Ready, Finish)
│       ├── handler/              # ContestWebSocketHandler (STOMP)
│       ├── dto/                  # DTO для REST и WebSocket
│       └── util/                 # WebsocketTopicStorage
├── security/                     # JWT, SecurityConfig, WebSocket auth, RefreshToken
├── scheduler/                    # ContentCleanupService, TokenRefreshCleanupService
├── exception/, handler/          # Обработка ошибок, ErrorCode
└── properties/                   # CustomProperties (CORS, фронт, и т.д.)
```

---

## Структура фронтенда

```
k1-typing-frontend/src/
├── api/              # auth, axios, exercises, multiplayer, resultItem, user
├── components/       # CreateRoomModal, ExerciseCard, GlassCard, HandDiagram, PlayerProgressBar, RoomCard, VirtualKeyboard
├── views/            # Home, Exercise, ContestRoom, Login, Register, Profile
├── stores/           # auth, contest, exercises, multiplayer, typing (Pinia)
├── services/         # tokenStorage, websocket (STOMP)
├── router/
└── assets/styles/
```

---

## Запуск проекта

### Требования

- Docker и Docker Compose  
- (Опционально) Java 21, Node 20+, Maven — для локальной разработки без Docker

### Запуск через Docker Compose

Скопируйте `.env.origin` в `.env` и задайте переменные

   ```bash
   docker-compose up -d --build
   ```

### Локальная разработка (без Docker)

- **Backend**: из `k1-typing-backend` запускать `./mvnw spring-boot:run`. Нужны запущенные PostgreSQL и Redis (или Docker только для них). Параметры — в `application.yaml` / env.
- **Frontend**: из `k1-typing-frontend` — `npm install && npm run dev`. Настроить `VITE_API_BASE_URL` и `VITE_WEBSOCKET_URL` на адрес бэкенда.

---

## CI/CD и деплой

- **CI**: скрипт `ci.sh` — тесты и SonarQube (`./mvnw clean verify sonar:sonar`) в `k1-typing-backend`. Рассчитан на ветку `dev`; при успехе — интерактивный коммит.
- **Деплой**: GitHub Actions workflow `/.github/workflows/deploy.yml` при пуше в `main` (или ручной запуск). Подключается по SSH к VPS, в каталоге `/home/deployer/k1-typing` выполняет `git pull`, `docker-compose down`, `docker-compose up -d --build`, очистку образов.

Секреты: `HOST`, `USERNAME`, `SSH_PRIVATE_KEY` в настройках репозитория.

