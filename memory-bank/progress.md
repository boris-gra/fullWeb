# Progress — fullWeb (Query)

## Что работает
- Приложение развёрнуто: query-gra.koyeb.app и query-gra.web.app.
- Docker-образ borisgra/query-gra.
- Подключения к PostgreSQL: chinook (biganimal), local, querym.

## Что осталось / известные особенности
- Старт на web.app занимает ~3.5 сек (холодный старт).
- **Задержка при открытии меню (`open_menu` / `cdp-open-menu.ps1`): возможна до 10 сек из-за холодного старта Google Run.** После клика по пункту меню может отобразиться «Trying to fetch...» / 0 строк — это НЕ ошибка; нужно подождать (до ~10 сек) и повторно проверить состояние страницы.
- Внутренняя реализация уточняется по коду (main/, java/, shared/) и `src/commonMain/resources/help.html`.

## История решений
- 20.08.2026: создан переиспользуемый скрипт `.cline_scripts/cdp-print-to-pdf.ps1` — конвертация HTML → PDF через CDP (Page.printToPDF). Использован для сохранения «Предложения по Main.pdf».
- Проект построен по образцу JetBrains jvm-js-fullstack / web-app-react-kotlin-js-gradle.
- Для отображения данных используется ag-grid-community.
- Скриншоты вкладок делаются БЕЗ browser-bridge, через CDP (`--remote-debugging-port=9222`): проверенный PowerShell-скрипт сохранён в `.clinerules/project.md`. Причина: browser-bridge отдаёт base64 большими объёмами/обрезает результат; CDP-рецепт надёжен.
