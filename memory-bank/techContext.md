# Tech Context — fullWeb (Query)

## Стек
- Kotlin Multiplatform, Kotlin 2.4.10 (kotlin("multiplatform"), kotlin("jvm"), kotlin("plugin.serialization")).
- Kotlin/JS + React (kotlin-wrappers), MUI.
- npm: ag-grid-community, react-share, mui-nested-menu, browserconnect ^1.1.6.
- Gradle (kotlin-js-store), Java 25 (system.properties).

## Окружение разработки
- Windows, VS Code + расширение Cline.
- Браузер запускается с `--remote-debugging-port=9222` → доступен Chrome DevTools Protocol (CDP) на `http://127.0.0.1:9222`.
- Скриншоты вкладок (ОСНОВНОЙ способ, БЕЗ browser-bridge): полный проверенный PowerShell-скрипт в `.clinerules/project.md` — `Page.captureScreenshot` через WebSocket (`webSocketDebuggerUrl` из `/json/list`), автонумерация и сохранение в `F:/Projects_git/fullWeb/ScreenNNN.png`, открытие в новой вкладке через `PUT /json/new?file:///...`.
- Браузерный мост (альтернатива, НЕ по умолчанию): расширение browserconnect (документация: `node_modules/browserconnect/CLAUDE.md`), инструменты `browser-bridge__browser_*`. Использовать только по явному запросу пользователя или если CDP недоступен.
- Список открытых вкладок браузера получать динамически через `browser-bridge__browser_get_tabs` или `http://127.0.0.1:9222/json/list`.

## Переменные окружения
- ADMIN_PASSW, DATABASE_TIMEOUT, DATABASE_URL_animal, DATABASE_URL_local, DATABASE_URL_querym, QUERY_BD, q — см. projectbrief.md.

## Инструменты
- `gradlew` / `gradlew.bat`, Docker, gcloud (Cloud Run), Koyeb, Firebase.
