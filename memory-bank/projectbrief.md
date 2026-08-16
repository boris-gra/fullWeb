# Project Brief — fullWeb (Query)

## Цель
Fullstack веб-приложение на Kotlin Multiplatform, которое позволяет «Look and modify ANY view from ANY base» — просматривать и модифицировать представления (views) в базах данных PostgreSQL и Google BigQuery.

## Прод-ссылки
- https://query-gra.koyeb.app
- https://query-gra.web.app (Firebase, холодный старт ~3.5 сек)
- Docker-образ: borisgra/query-gra

## Окружение (Config Vars)
| Переменная | Назначение |
|---|---|
| ADMIN_PASSW | пароль администратора |
| DATABASE_TIMEOUT | таймаут БД (15) |
| DATABASE_URL_animal | Postgres chinook (biganimal) |
| DATABASE_URL_local | локальный Postgres |
| DATABASE_URL_querym | рабочая БД (querym) |
| QUERY_BD | рабочая база = `querym` |
| q | рабочее представление = `v_history_last50_ro` |

## Справка
- `src/commonMain/resources/help.html`
- Описание запросов — в представлении QUERY_BD (меню Construction → Development → Queries).
