# Progress — fullWeb (Query)

## Что работает
- Приложение развёрнуто: query-gra.koyeb.app и query-gra.web.app.
- Docker-образ borisgra/query-gra.
- Подключения к PostgreSQL: chinook (biganimal), local, querym.

## Что осталось / известные особенности
- Старт на web.app занимает ~3.5 сек (холодный старт).
- Внутренняя реализация уточняется по коду (main/, java/, shared/) и `src/commonMain/resources/help.html`.

## История решений
- Проект построен по образцу JetBrains jvm-js-fullstack / web-app-react-kotlin-js-gradle.
- Для отображения данных используется ag-grid-community.
- Скриншоты вкладок делаются БЕЗ browser-bridge, через CDP (`--remote-debugging-port=9222`): проверенный PowerShell-скрипт сохранён в `.clinerules/project.md`. Причина: browser-bridge отдаёт base64 большими объёмами/обрезает результат; CDP-рецепт надёжен.
