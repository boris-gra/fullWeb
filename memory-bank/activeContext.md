# Active Context — fullWeb (Query)

## Текущий фокус
- Настройка постоянной памяти Cline (Memory Bank + правила `.clinerules/`), чтобы контекст проекта сохранялся между чатами.

## Последние действия
- Навигация в приложении через CDP (вкладка «Querys», https://query.boris-gra.xyz/): клик по кнопке MENU → наведение на «All Query» → наведение на «Feed (querym)» → клик по «foods All». Подменю раскрываются наведением (mouseenter/mouseover через Runtime.evaluate). Результат: таблица foods (402 строки, 22 страницы). Скриншот: `Screen004.png` (открыт в новой вкладке браузера).
- Ещё навигация: MENU → All Query → Feed (querym) → «Clients - fullWeb». Результат: таблица clients (34 строки, 2 страницы), колонки id, pers, STATUS !, person_name, user_mail, write_date, android_id, Write User. Скриншот: `Screen005.png`.
- Создан универсальный PowerShell-скрипт `.cline_scripts/cdp-open-menu.ps1` (папка `.cline_scripts` для скриптов Cline в корне проекта): раскрывает любую ветку меню через CDP (клик MENU + наведения по подменю, клик последнего пункта) БЕЗ скриншота. Параметры: `-MenuPath` (массив пунктов), `-TabPattern`, `-Port`, `-WaitMs`, `-HoverLast` (последний пункт только наводить). Пример: `.\cline_scripts\cdp-open-menu.ps1 -MenuPath @('MENU','All Query','Feed (querym)','Clients - fullWeb')`. Проверен на ветках foods All (402) и Clients - fullWeb (34).
- Замечание: PowerShell в этом окружении не поддерживает формат-спецификаторы `{0:D3}`/`ToString('D3')` — нумерацию ScreenNNN делать через `.ToString().PadLeft(3,'0')`.
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
- Логи чата: папка `.cline_chat-logs`, имя `chat-log-YYYY-MM-DD-HH-MM.txt` (HH-MM = время начала чата). Лог формировать для каждого чата ТОЛЬКО если пользователь попросил об этом в начале чата; иначе не формировать. Правило — в `.clinerules/project.md`.
- Пример лога: `.cline_chat-logs/chat-log-2026-08-17-11-39.txt` (текущий чат).
