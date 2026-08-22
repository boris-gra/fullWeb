# dsh-usage.ps1 — расход токенов и $ по сессиям DeepSeek Harness + баланс счёта.
# Обёртка над .cline_scripts/dsh-usage.mjs. Запуск:
#   .\.cline_scripts\dsh-usage.ps1            # вся сводка (токены/$ + баланс)
#   .\.cline_scripts\dsh-usage.ps1 -Tokens    # только токены/$
#   .\.cline_scripts\dsh-usage.ps1 -Balance   # только баланс счёта
param(
    [switch]$Tokens,
    [switch]$Balance
)
$ErrorActionPreference = 'Stop'
$dir = Split-Path -Parent $MyInvocation.MyCommand.Path
$script = Join-Path $dir 'dsh-usage.mjs'
if (-not (Test-Path $script)) { throw "Не найден $script" }

$args = @()
if ($Tokens) { $args += '--tokens' }
if ($Balance) { $args += '--balance' }
if (-not $Tokens -and -not $Balance) { $args = @() } # вся сводка

# прозрачно передать цены, если заданы хотя бы одна DS_PRICE_*
node $script @args
exit $LASTEXITCODE
