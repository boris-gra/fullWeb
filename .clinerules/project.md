# Правила проекта fullWeb

## Общие правила
- Отвечай пользователю на русском языке.
- Перед началом любой задачи прочитай все файлы из `memory-bank/` и опирайся на них.
- После завершения значимой задачи обнови `memory-bank/activeContext.md`, а при необходимости — `memory-bank/progress.md`.
- Не выдумывай факты о проекте: если деталь неизвестна — посмотри в коде или спроси пользователя.

## Логи чата
- Папка для логов чата: `.cline_chat-logs` в корне проекта.
- Имя файла: `chat-log-YYYY-MM-DD-HH-MM.txt`, где `HH-MM` — время НАЧАЛА чата (24-часовой формат, hh:mm). Пример: `chat-log-2026-08-17-11-39.txt`.
- Chat-log формировать для КАЖДОГО чата, НО только если пользователь в начале чата попросил вести лог. Если в начале чата не сказано — лог НЕ формировать.
- Формат лога: заголовок (дата + время начала), далее по порядку сообщения пользователя и ответы Cline, в конце — итоги сессии (что сделано, скриншоты, данные, обновлённые файлы).

## Работа с браузером (скриншоты и т.п.)

### Договорённости по скриншотам (важно!)
- Скриншоты показывать пользователю в ДЕКОДИРОВАННОМ виде (картинкой), сырые base64-данные в чат НЕ выводить.
- Способ показа: сохранить PNG в файл, затем открыть его в новой вкладке браузера через `file:///...` URL. В чате указывать путь к файлу (допустима markdown-ссылка `![...](путь)`).
- Имена файлов — `ScreenNNN.png`, где NNN — порядковый номер (001, 002, 003, ...). Перед сохранением проверять существующие `Screen*.png` в корне проекта, чтобы взять следующий номер.
- **По умолчанию скриншоты делать через CDP БЕЗ browser-bridge** (см. полный рецепт ниже). browser-bridge использовать ТОЛЬКО по явному запросу пользователя или если CDP недоступен.

### Основной способ (по умолчанию, БЕЗ browser-bridge): скриншот через CDP
Браузер запущен с флагом `--remote-debugging-port=9222` (Chrome DevTools Protocol на `http://127.0.0.1:9222`).

Шаги:
1. Найти вкладку: `Invoke-RestMethod -Uri 'http://127.0.0.1:9222/json/list'`, выбрать `type -eq 'page'` по `title`/`url`, взять `webSocketDebuggerUrl`.
2. Подключиться по WebSocket (System.Net.WebSockets.ClientWebSocket) к этой URL и вызвать `Page.captureScreenshot` (params `{format:'png'}`).
3. Собрать ответ (возможно приходит несколькими чанками, читать до `EndOfMessage`), `result.data` (base64) декодировать (`[Convert]::FromBase64String`) и записать в `F:/Projects_git/fullWeb/ScreenNNN.png`.
4. Открыть в новой вкладке: `Invoke-RestMethod -Method Put -Uri "http://127.0.0.1:9222/json/new?<EscapeDataString(file:///F:/Projects_git/fullWeb/ScreenNNN.png)>"`.

**Проверенный рабочий скрипт (PowerShell)** — перед запуском заменить `$TargetPattern` на regex по `title`/`url` нужной вкладки:
```powershell
# === Скриншот вкладки БЕЗ browser-bridge (CDP, порт 9222) ===
$ErrorActionPreference = 'Stop'
$TargetPattern = '(?i)news\.google\.com'   # ← ЗАМЕНИТЬ под нужную вкладку (regex по title/url)

# Следующий номер ScreenNNN
$existing = Get-ChildItem -Path 'F:/Projects_git/fullWeb' -Filter 'Screen*.png'
$nextNum = 1
if ($existing) { $nextNum = ($existing | ForEach-Object { [int]($_.BaseName -replace 'Screen','') } | Measure-Object -Maximum).Maximum + 1 }
$outFile = 'F:/Projects_git/fullWeb/Screen{0:D3}.png' -f $nextNum

# Найти вкладку
$tabs = Invoke-RestMethod -Uri 'http://127.0.0.1:9222/json/list'
$tab = $tabs | Where-Object { $_.type -eq 'page' -and ($_.title -match $TargetPattern -or $_.url -match $TargetPattern) } | Select-Object -First 1
if (-not $tab) { Write-Error "Вкладка не найдена по паттерну: $TargetPattern" }

# WebSocket → Page.captureScreenshot
$ws = New-Object System.Net.WebSockets.ClientWebSocket
$ws.ConnectAsync([System.Uri]::new($tab.webSocketDebuggerUrl), [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$cmd = @{ id = 1; method = 'Page.captureScreenshot'; params = @{ format = 'png' } } | ConvertTo-Json -Compress
$bytes = [System.Text.Encoding]::UTF8.GetBytes($cmd)
$ws.SendAsync([System.ArraySegment[byte]]::new($bytes), [System.Net.WebSockets.WebSocketMessageType]::Text, $true, [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
$buffer = New-Object byte[] 20971520
$mem = New-Object System.IO.MemoryStream
$ct = [System.Threading.CancellationToken]::None
do {
  $seg = [System.ArraySegment[byte]]::new($buffer)
  $r = $ws.ReceiveAsync($seg, $ct).GetAwaiter().GetResult()
  $mem.Write($buffer, 0, $r.Count)
} while (-not $r.EndOfMessage)
$ws.Dispose()

# Декодировать base64 → PNG
$resp = ([System.Text.Encoding]::UTF8.GetString($mem.ToArray())) | ConvertFrom-Json
if ($resp.error) { Write-Error "CDP error: $($resp.error.message)" }
[System.IO.File]::WriteAllBytes($outFile, [System.Convert]::FromBase64String($resp.result.data))
Write-Host "Saved: $outFile"

# Открыть в новой вкладке
$uri = [uri]::EscapeDataString('file:///' + ($outFile -replace '\\','/'))
Invoke-RestMethod -Method Put -Uri "http://127.0.0.1:9222/json/new?$uri" | Out-Null
Write-Host 'Opened in new tab'
```
Полезные проверки после сохранения:
- Размер/факт файла: `Get-Item <путь> | Select-Object Name,Length,LastWriteTime`.
- Что вкладка с файлом открылась: из `/json/list` отфильтровать `url -like 'file://*Screen*'`.
- Список открытых вкладок (без browser-bridge): `Invoke-RestMethod -Uri 'http://127.0.0.1:9222/json/list'`.

### Навигация по меню приложения Query (клики/наведение, БЕЗ скриншота)
- Готовый скрипт: `.cline_scripts/cdp-open-menu.ps1` (папка `.cline_scripts` в корне проекта). Раскрывает любую ветку меню в вкладке «Querys» через CDP: клик по MENU, наведения (mouseenter/mouseover) по подменю, клик последнего пункта. Скриншот не делает.
- Запуск: `.\cline_scripts\cdp-open-menu.ps1 -MenuPath @('MENU','All Query','Feed (querym)','foods All')`
- Ключевые параметры: `-MenuPath` (массив пунктов), `-TabPattern` (по умолчанию `'(?i)^querys$'`), `-HoverLast` (последний пункт только навести, без клика), `-WaitMs`.
- Если нужен только скриншот текущего состояния после навигации — вызвать скрипт из раздела выше.

### Альтернатива (НЕ по умолчанию): browser-bridge
- Инструменты `browser-bridge__browser_get_tabs`, `browser-bridge__browser_screenshot`, `browser-bridge__browser_navigate` (расширение browserconnect). Использовать только по явному запросу пользователя или если CDP недоступен.
- Вкладку находить по `title`/`url` через `browser-bridge__browser_get_tabs`.
- Скриншот возвращается как base64 — в чат его НЕ выводить; при необходимости сохранить в файл по правилам выше.
- Полезное: документация расширения browserconnect — `node_modules/browserconnect/CLAUDE.md`.
- Вкладка «Querys» в браузере = рабочее приложение Query (https://query.boris-gra.xyz/).

## Проект (кратко)
- fullWeb = приложение «Query»: Kotlin Multiplatform fullstack (Kotlin 2.4.10).
- Модули: `main` (JS/Frontend, React + MUI), `java` (JVM/Backend, запускается через Procfile как `query-gra`), `shared` (общий код).
- Назначение: «Look and modify ANY view from ANY base» — просмотр и редактирование представлений в PostgreSQL / Google BigQuery.
- npm: ag-grid-community, react-share, mui-nested-menu, browserconnect.
- Деплой: https://query-gra.koyeb.app, https://query-gra.web.app (старт ~3.5 сек), Docker: borisgra/query-gra.
- Справка приложения: `src/commonMain/resources/help.html`.
- Подробнее: `memory-bank/`, `README.md`.
