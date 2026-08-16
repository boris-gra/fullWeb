# Active Context — fullWeb (Query)

## Текущий фокус
- Настройка постоянной памяти Cline (Memory Bank + правила `.clinerules/`), чтобы контекст проекта сохранялся между чатами.

## Последние действия
- Получен скриншот вкладки «Querys» (https://query.boris-gra.xyz/) через browser-bridge: `browser_get_tabs` → найдена вкладка по title «Querys» → `browser_screenshot` с tabId.
- Создан Memory Bank (`memory-bank/`) и правила проекта (`.clinerules/`).
- Настроен показ скриншотов через CDP: браузер запущен с `--remote-debugging-port=9222`. Скриншот вкладки снимается через `Page.captureScreenshot`, сохраняется в `ScreenNNN.png` и открывается в новой вкладке браузера (`file:///...`).
- Договорённость с пользователем: файлы скриншотов называть `ScreenNNN.png` (NNN — порядковый номер); base64 в чат не выводить, показывать картинку декодированно (в новой вкладке браузера + путь в чате).
- Скриншоты через browser-bridge получаются обрезанными/с большим base64 в чат; пользователь явно попросил снимать скриншоты БЕЗ browser-bridge. Проверенный рабочий PowerShell-скрипт (CDP, `Page.captureScreenshot`, WebSocket, автонумерация `ScreenNNN.png`, открытие в новой вкладке) сохранён в `.clinerules/project.md` — использовать его по умолчанию в следующих чатах.
- Сняты скриншоты по этому рецепту: `Screen002.png` (Google Новости, news.google.com), `Screen003.png` (Brent crude oil, tradingeconomics.com). Уже есть также `Screen001.png` (Querys, старый способ).

## Следующие шаги
- Продолжать работу над приложением Query по запросам пользователя.
- Обновлять этот файл после каждой сессии: что делали, что дальше.

## Важно для будущих сессий
- Общаться с пользователем на русском языке.
- Вкладка «Querys» в браузере — это рабочее приложение (https://query.boris-gra.xyz/).
- Скриншоты вкладок делать ТОЛЬКО через CDP (порт 9222) по рецепту из `.clinerules/project.md`, НЕ через browser-bridge.
