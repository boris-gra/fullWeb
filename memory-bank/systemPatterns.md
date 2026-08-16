# System Patterns — fullWeb (Query)

## Модули Gradle (settings.gradle.kts: rootProject "fullWeb")
- `main` — JS/Frontend (React + MUI).
- `java` — JVM/Backend; Procfile запускает `./java/build/install/query-gra/bin/query-gra` (имя приложения `query-gra`).
- `shared` — общий код (KMP).

## Frontend
- Kotlin/JS + React (kotlin-wrappers), Material UI.
- npm: ag-grid-community (таблицы), react-share, mui-nested-menu, browserconnect (только для dev-инструментов).

## Backend / БД
- PostgreSQL, Google BigQuery. Источники — через переменные окружения (projectbrief.md).

## Деплой
- Dockerfile, Dockerfile-git, Dockerfile-git-jdk, Dockerfile-git-run, Dockerfile-my-jdk, compose.yaml, Procfile, system.properties (java.runtime.version=25).
- Облако: google-cloud-run.yaml, gcp_cloud_build_fullweb.yaml, gcp_cloud_build_query.yaml, .env.
