---
name: open-menu
description: >-
  Навигация по меню приложения Query (https://query.boris-gra.xyz/, вкладка «Querys») через Chrome
  DevTools Protocol (CDP, порт 9222): клик по кнопке MENU, раскрытие подменю наведением курсора
  (mouseenter/mouseover) и клик последнего пункта ветки. Используй, когда пользователь просит открыть
  или раскрыть пункт меню в приложении Query (например «открой foods All», «покажи Clients - fullWeb»,
  «раскрой подменю Feed (querym)») или нужно навести курсор на пункт подменю. Базируется на скрипте
  .cline_scripts/cdp-open-menu.ps1. Скриншоты НЕ делает (для них — отдельный CDP-рецепт в
  .clinerules/project.md).
---

# Skill: open-menu

## Назначение
Раскрывает ветку меню приложения Query (https://query.boris-gra.xyz/, вкладка «Querys») через Chrome DevTools Protocol (CDP): кликает кнопку MENU, наводит курсор (mouseenter/mouseover) на промежуточные пункты подменю и кликает последний пункт ветки. Скриншот НЕ делается.

## Когда использовать
- Пользователь просит открыть/перейти к пункту меню в приложении Query (например: «открой foods All», «покажи Clients - fullWeb»).
- Нужно раскрыть подменю и оставить последний пункт наведённым — параметр `-HoverLast`.
- Как подготовительный шаг перед скриншотом таблицы (сам скриншот затем делать по CDP-рецепту из `.clinerules/project.md`).

## Как использовать
1. Определи ветку меню `MenuPath`:
   - `@('MENU','All Query','Feed (querym)','foods All')`
   - `@('MENU','All Query','Feed (querym)','Clients - fullWeb')`
   - `@('MENU','All Query','Feed (querym)') -HoverLast` — последний пункт только навести, без клика.
2. Проверь, что существует `.cline_scripts/cdp-open-menu.ps1`. Если файла нет — создай его, скопировав код из раздела «Скрипт» ниже (без заголовка и обрамления).
3. Запусти из корня проекта (например через `run_commands`):
   `.\cline_scripts\cdp-open-menu.ps1 -MenuPath @('MENU','All Query','Feed (querym)','foods All')`
4. Прочитай вывод. После клика скрипт печатает строку вида `Результат: {"agRows":…,"pager":…,"head":…}`:
   - `agRows` — сколько строк отрисовано в AG-Grid;
   - `pager` — текст пейджера таблицы;
   - `head` — первые строки страницы.
   Кратко перескажи результат пользователю.

## Параметры скрипта
| Параметр | Тип | По умолчанию | Описание |
|---|---|---|---|
| `MenuPath` | `string[]` | `@('MENU','All Query','Feed (querym)','foods All')` | Ветка меню: первый пункт — кнопка `MENU`, далее пункты подменю |
| `TabPattern` | `string` | `'(?i)^querys$'` | Regex поиска вкладки по title/url |
| `Port` | `int` | `9222` | Порт CDP |
| `WaitMs` | `int` | `1000` | Задержка между шагами, мс |
| `HoverLast` | `switch` | — | Последний пункт только навести, без клика |

## Требования
- Браузер запущен с `--remote-debugging-port=9222` (CDP доступен на `http://127.0.0.1:9222`).
- Вкладка приложения Query открыта в браузере (по умолчанию ищется по title/url `querys`; другую вкладку задать через `TabPattern`).

## Возможные ошибки
- «Вкладка не найдена по паттерну» — проверить открытые вкладки: `Invoke-RestMethod -Uri 'http://127.0.0.1:9222/json/list'`; при необходимости указать другой `TabPattern`.
- «Пункт '…' не найден. Доступные пункты: …» — скрипт выводит реальные пункты текущего подменю; уточнить `MenuPath`.
- «Кнопка MENU не найдена» — меню закрыто или разметка изменилась; сообщить пользователю.

## Скрипт
Полная копия `.cline_scripts/cdp-open-menu.ps1` (используется для воссоздания файла, если он отсутствует):

```powershell
#Requires -Version 5.1
<#
.SYNOPSIS
  Раскрывает ветку меню приложения Query (https://query.boris-gra.xyz/) через Chrome DevTools Protocol (CDP)
  и кликает последний пункт ветки. Скриншот НЕ делается.

.DESCRIPTION
  Скрипт подключается к браузеру по CDP (http://127.0.0.1:9222), находит вкладку приложения («Querys»),
  закрывает открытое меню (если было), кликает кнопку MENU, наводит курсор (mouseenter/mouseover) на
  промежуточные пункты подменю и кликает последний пункт ветки. Подменю раскрываются наведением курсора.

.PARAMETER MenuPath
  Ветка меню: массив пунктов. По умолчанию: MENU, All Query, Feed (querym), foods All.
  Примеры: @('MENU','All Query','Feed (querym)','Clients - fullWeb'); @('MENU','All Query','Feed (querym)','foods >= 464')

.PARAMETER TabPattern
  Regex поиска вкладки по title/url. По умолчанию: '(?i)^querys$'.

.PARAMETER Port
  Порт CDP. По умолчанию 9222.

.PARAMETER WaitMs
  Задержка между шагами, мс. По умолчанию 1000.

.PARAMETER HoverLast
  Если указан — последний пункт только наводится (без клика), подменю остаётся раскрытым.

.EXAMPLE
  .\cdp-open-menu.ps1
  .\cdp-open-menu.ps1 -MenuPath @('MENU','All Query','Feed (querym)','Clients - fullWeb')
  .\cdp-open-menu.ps1 -MenuPath @('MENU','All Query','Feed (querym)') -HoverLast
#>
param(
    [string[]]$MenuPath = @('MENU', 'All Query', 'Feed (querym)', 'foods All'),
    [string]$TabPattern = '(?i)^querys$',
    [int]$Port = 9222,
    [int]$WaitMs = 1000,
    [switch]$HoverLast
)

$ErrorActionPreference = 'Stop'

if ($MenuPath.Count -lt 2) {
    throw 'MenuPath должен содержать минимум 2 пункта (например MENU и целевой пункт).'
}

function Send-Cdp {
    param($Ws, $Id, $Method, $Params)
    $cmd = @{ id = $Id; method = $Method; params = $Params } | ConvertTo-Json -Compress -Depth 10
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($cmd)
    $Ws.SendAsync([System.ArraySegment[byte]]::new($bytes), [System.Net.WebSockets.WebSocketMessageType]::Text, $true,
        [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()
    $buffer = New-Object byte[] 8388608
    $mem = New-Object System.IO.MemoryStream
    $ct = [System.Threading.CancellationToken]::None
    do {
        $seg = [System.ArraySegment[byte]]::new($buffer)
        $r = $Ws.ReceiveAsync($seg, $ct).GetAwaiter().GetResult()
        $mem.Write($buffer, 0, $r.Count)
    } while (-not $r.EndOfMessage)
    return ([System.Text.Encoding]::UTF8.GetString($mem.ToArray())) | ConvertFrom-Json
}

function Invoke-Eval {
    param($Ws, $Id, $Expression)
    $resp = Send-Cdp $Ws $Id 'Runtime.evaluate' @{ expression = $Expression; returnByValue = $true }
    if ($resp.result.exceptionDetails) {
        throw "JS error: $($resp.result.exceptionDetails.exception.value.text)"
    }
    return $resp.result.result.value
}

# Экранирование текста пункта меню для встраивания в JS-строку в одинарных кавычках
function Esc-JsString {
    param([string]$S)
    return $S.Replace('\', '\\').Replace("'", "\'")
}

function Test-ElementExists {
    param($Ws, $Id, $Text)
    $t = Esc-JsString $Text
    $js = "(()=>{const t='$t'; return [...document.querySelectorAll('[role=menuitem]')].some(x=>(x.innerText||'').trim()===t);})()"
    return [bool](Invoke-Eval $Ws $Id $js)
}

Write-Host "Ищем вкладку по паттерну: $TabPattern" -ForegroundColor Cyan
$tabs = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/json/list"
$tab = $tabs | Where-Object { $_.type -eq 'page' -and ($_.title -match $TabPattern -or $_.url -match $TabPattern) } |
    Select-Object -First 1
if (-not $tab) { throw "Вкладка не найдена по паттерну: $TabPattern" }
Write-Host "Вкладка: $($tab.title)  ($($tab.url))" -ForegroundColor Cyan

$ws = New-Object System.Net.WebSockets.ClientWebSocket
[void]$ws.ConnectAsync([System.Uri]::new($tab.webSocketDebuggerUrl), [System.Threading.CancellationToken]::None).GetAwaiter().GetResult()

try {
    $id = 1

    # 1) Если меню уже открыто — закрыть, чтобы стартовать с чистого состояния
    $menuOpen = [bool](Invoke-Eval $ws $id "(()=>document.querySelectorAll('[role=menuitem]').length>0)()")
    $id++
    if ($menuOpen) {
        Invoke-Eval $ws $id "(()=>{const b=[...document.querySelectorAll('button')].find(x=>(x.innerText||'').trim()==='MENU'); if(b)b.click(); return true;})()" | Out-Null
        $id++
        Start-Sleep -Milliseconds 800
        Write-Host 'Закрыто ранее открытое меню' -ForegroundColor Yellow
    }

    # 2) Клик по первому пункту ветки (ожидаем кнопку MENU)
    if ($MenuPath[0] -eq 'MENU') {
        $clicked = [bool](Invoke-Eval $ws $id "(()=>{const b=[...document.querySelectorAll('button')].find(x=>(x.innerText||'').trim()==='MENU'); if(!b)return false; b.dispatchEvent(new MouseEvent('mousedown',{bubbles:true})); b.dispatchEvent(new MouseEvent('mouseup',{bubbles:true})); b.click(); return true;})()")
        $id++
        if (-not $clicked) { throw 'Кнопка MENU не найдена' }
        Write-Host "[1/$($MenuPath.Count)] Клик: MENU" -ForegroundColor Green
        Start-Sleep -Milliseconds $WaitMs
    }

    # 3) Промежуточные пункты — наведение; последний — клик (если не -HoverLast)
    for ($i = 1; $i -lt $MenuPath.Count; $i++) {
        $item = $MenuPath[$i]
        $isLast = ($i -eq $MenuPath.Count - 1)
        $doClick = $isLast -and -not $HoverLast

        # Ожидание появления пункта в DOM (подменю раскрывается после наведения)
        $found = $false
        $deadline = (Get-Date).AddSeconds(10)
        while (-not $found -and (Get-Date) -lt $deadline) {
            $found = Test-ElementExists $ws $id $item
            if (-not $found) {
                $id++
                Start-Sleep -Milliseconds 300
            }
        }
        if (-not $found) {
            $list = Invoke-Eval $ws $id "(()=>[...document.querySelectorAll('[role=menuitem]')].map(x=>(x.innerText||'').trim()).join(' | '))()"
            throw "Пункт '$item' не найден. Доступные пункты: $list"
        }

        $t = Esc-JsString $item
        if ($doClick) {
            $js = "(()=>{const t='$t'; const el=[...document.querySelectorAll('[role=menuitem]')].find(x=>(x.innerText||'').trim()===t); if(!el)return false; const r=el.getBoundingClientRect(); const cx=r.x+r.width/2, cy=r.y+r.height/2; ['pointerover','mouseover','pointerenter','mouseenter'].forEach(ev=>el.dispatchEvent(new MouseEvent(ev,{bubbles:true,clientX:cx,clientY:cy}))); el.dispatchEvent(new MouseEvent('mousedown',{bubbles:true,clientX:cx,clientY:cy})); el.dispatchEvent(new MouseEvent('mouseup',{bubbles:true,clientX:cx,clientY:cy})); el.click(); return true;})()"
            $res = [bool](Invoke-Eval $ws $id $js)
            $id++
            if (-not $res) { throw "Не удалось кликнуть '$item'" }
            Write-Host "[$($i + 1)/$($MenuPath.Count)] Клик: $item" -ForegroundColor Green
        } else {
            $js = "(()=>{const t='$t'; const el=[...document.querySelectorAll('[role=menuitem]')].find(x=>(x.innerText||'').trim()===t); if(!el)return false; const r=el.getBoundingClientRect(); const cx=r.x+r.width/2, cy=r.y+r.height/2; ['pointerover','mouseover','pointerenter','mouseenter'].forEach(ev=>el.dispatchEvent(new MouseEvent(ev,{bubbles:true,clientX:cx,clientY:cy}))); return true;})()"
            $res = [bool](Invoke-Eval $ws $id $js)
            $id++
            if (-not $res) { throw "Не удалось навести на '$item'" }
            Write-Host "[$($i + 1)/$($MenuPath.Count)] Наведение: $item" -ForegroundColor Yellow
            Start-Sleep -Milliseconds $WaitMs
        }
    }

    # 4) Сводка результата (данные в AG-Grid после клика)
    if (-not $HoverLast) {
        Start-Sleep -Seconds 5
        $info = Invoke-Eval $ws $id "(()=>{const rows=document.querySelectorAll('.ag-row').length; const pager=document.querySelector('.ag-paging-row-summary-panel'); const head=document.body.innerText.split('\n').filter(x=>x.trim()).slice(0,3).join(' | '); return JSON.stringify({agRows:rows, pager:pager?pager.innerText:null, head:head});})()"
        Write-Host "Результат: $info" -ForegroundColor Magenta
    }

    Write-Host 'Готово (скриншот не делается).' -ForegroundColor Green
}
finally {
    $ws.Dispose()
}
```
