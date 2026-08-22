#!/usr/bin/env node
/*
 * dsh-usage.mjs — расход токенов и $ по сессиям DeepSeek Harness + баланс счёта.
 *
 * Требуется Node >= 22.5 (node:zlib zstd). Внешних пакетов нет.
 *
 * Запуск:
 *   node .cline_scripts/dsh-usage.mjs            # все сессии + баланс
 *   node .cline_scripts/dsh-usage.mjs --tokens   # только токены/$
 *   node .cline_scripts/dsh-usage.mjs --balance  # только баланс счёта
 *
 * Цены (долларов за 1M токенов) задаются env-переменными; при отсутствии —
 * дефолты. Цены DeepSeek менялись, проверьте актуальные:
 *   https://api-docs.deepseek.com/quick_start/pricing
 */
// import { createRequire } from 'node:module'
import { zstdDecompressSync } from 'node:zlib'
import { readFileSync, readdirSync } from 'node:fs'
import { homedir } from 'node:os'
import { join, basename, dirname, relative } from 'node:path'
import { fileURLToPath } from 'node:url'

// const require = createRequire(import.meta.url)
// const fs = require('node:fs')

/* Минимальный загрузчик .env (фолбэк ПРОЕКТА, низший приоритет). Никогда не
   переопределяет уже заданную переменную окружения. Позволяет объявить все
   DS_PRICE_* и DS_* прямо в корневом .env проекта (на уровень выше .cline_scripts), не трогая шелл.
   Путь по умолчанию: <проект>/.env (можно переопределить через DS_DOTENV). */
function loadDotEnv() {
  const map = {}
  const file = process.env.DS_DOTENV !== undefined
    ? process.env.DS_DOTENV
    : join(dirname(fileURLToPath(import.meta.url)), '..', '.env')
  try {
    const raw = readFileSync(file, 'utf8')
    for (const line of raw.split('\n')) {
      const m = line.match(/^\s*([A-Za-z_][A-Za-z0-9_]*)\s*=\s*(.*)\s*$/)
      if (!m) continue
      const key = m[1]
      if (process.env[key] !== undefined) continue // не переопределяем уже заданное
      let val = m[2].trim()
      val = val.replace(/[ \t]+#.*$/, '').trim()
      if ((val.startsWith('"') && val.endsWith('"')) || (val.startsWith("'") && val.endsWith("'"))) val = val.slice(1, -1)
      map[key] = val
    }
  } catch { /* .env отсутствует — не критично */ }
  return map
}
const cfg = (name, def) => process.env[name] !== undefined ? process.env[name] : (DOTENV[name] !== undefined ? DOTENV[name] : def)
const numCfg = (name, def) => Number(cfg(name, def))
const DOTENV = loadDotEnv()

const DSH_HOME = cfg('DSH_HOME', join(homedir(), '.dsh'))
const SESSIONS_ROOT = cfg('DS_SESSIONS', join(DSH_HOME, 'sessions'))
const CREDENTIALS = cfg('DS_CREDENTIALS', join(DSH_HOME, '.credentials.yaml'))

// ——— Цены, USD за 1M токенов ———
// DeepSeek: reasoning включён в output; cache-write не тарифицируется.
// Off-peak (базовые) — существующие DS_PRICE_*; peak — DS_PRICE_*_PEAK (если не заданы = off-peak).
const OFFPEAK = {
  input: numCfg('DS_PRICE_INPUT', 0.22),            // 1M вход (cache miss, off-peak)
  cacheRead: numCfg('DS_PRICE_CACHE_READ', 0.007),  // 1M вход (cache hit, off-peak)
  cacheWrite: numCfg('DS_PRICE_CACHE_WRITE', 0.0),  // кэш-запись (не тарифицируется)
  output: numCfg('DS_PRICE_OUTPUT', 0.66),          // 1M выход (off-peak, reasoning включён)
}
const PEAK = {
  input: numCfg('DS_PRICE_INPUT_PEAK', OFFPEAK.input),
  cacheRead: numCfg('DS_PRICE_CACHE_READ_PEAK', OFFPEAK.cacheRead),
  cacheWrite: numCfg('DS_PRICE_CACHE_WRITE_PEAK', OFFPEAK.cacheWrite),
  output: numCfg('DS_PRICE_OUTPUT_PEAK', OFFPEAK.output),
}

// ——— График peak/off-peak по времени запроса ———
// Непиковой считается время в окне [start, end) в заданном часовом поясе, а также
// (опционально) все выходные. Определяется по временнóй метке каждого usage-события.
const TZ = cfg('DS_TZ', 'Europe/Kyiv')                      // часовой пояс расчёта (Украина)
const OFFPEAK_START = parseHHMM(cfg('DS_OFFPEAK_START', '00:30'))
const OFFPEAK_END = parseHHMM(cfg('DS_OFFPEAK_END', '08:30'))
const OFFPEAK_WEEKEND = cfg('DS_OFFPEAK_WEEKEND', 'true') !== 'false'

const WEEKDAY = { Sun: 0, Mon: 1, Tue: 2, Wed: 3, Thu: 4, Fri: 5, Sat: 6 }
function parseHHMM(s) { const m = String(s).match(/^(\d{1,2}):(\d{2})$/); return m ? (+m[1] * 60 + +m[2]) : 0 }
function tzParts(ms) {
  const parts = Object.fromEntries(new Intl.DateTimeFormat('en-US', {
    timeZone: TZ, hour12: false, hour: '2-digit', minute: '2-digit', weekday: 'short',
  }).formatToParts(new Date(ms)).map(p => [p.type, p.value]))
  return { hour: (+parts.hour) % 24, minute: +parts.minute, day: WEEKDAY[parts.weekday] }
}
function isOffPeak(ms) {
  const { hour, minute, day } = tzParts(ms)
  if (OFFPEAK_WEEKEND && (day === 0 || day === 6)) return true
  const minutes = hour * 60 + minute
  return minutes >= OFFPEAK_START && minutes < OFFPEAK_END
}
// const priceFor = ms => (ms !== undefined && !isOffPeak(ms)) ? PEAK : OFFPEAK
function usageCost(u, p) {
  return (u.inputTokens ?? 0) / 1e6 * p.input
    + (u.cacheReadTokens ?? 0) / 1e6 * p.cacheRead
    + (u.cacheWriteTokens ?? 0) / 1e6 * p.cacheWrite
    + (u.outputTokens ?? 0) / 1e6 * p.output
}
const fmtHM = m => `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`

const BALANCE_URL = 'https://api.deepseek.com/user/balance'

/* ---------------------------------- zstd frame scan ----------------------- */
// Перенос логики scanZstdFrames из session-persistence-jsonl (harness):
// сессия = конкатенация независимых zstd-фреймов. zstdDecompressSync декодирует
// только один фрейм, поэтому границы фреймов ищем вручную.
const ZSTD_MAGIC = 0xFD2FB528

function scanZstdFrames(buffer) {
  const frames = []
  let off = 0
  while (off < buffer.length) {
    const start = off
    if (buffer.length - off < 4) return { frames, tornStart: start }
    if (buffer.readUInt32LE(off) !== ZSTD_MAGIC) return { frames, tornStart: start, corrupt: `magic@${off}` }
    off += 4
    if (off === buffer.length) return { frames, tornStart: start }
    const descriptor = buffer.readUInt8(off)
    off += 1
    if ((descriptor & 0x18) !== 0) return { frames, tornStart: start, corrupt: `reserved@${off - 1}` }
    const csf = descriptor >>> 6
    const single = (descriptor & 0x20) !== 0
    const checksum = (descriptor & 0x04) !== 0
    const dict = descriptor & 0x03
    const dictBytes = dict === 3 ? 4 : dict
    const csBytes = csf === 0 ? (single ? 1 : 0) : (1 << csf)
    const hdr = (single ? 0 : 1) + dictBytes + csBytes
    if (buffer.length - off < hdr) return { frames, tornStart: start }
    off += hdr
    for (;;) {
      if (buffer.length - off < 3) return { frames, tornStart: start }
      const bh = buffer.readUIntLE(off, 3)
      off += 3
      const last = (bh & 1) !== 0
      const btype = (bh >>> 1) & 0x03
      const bsize = bh >>> 3
      if (btype === 0x03) return { frames, tornStart: start, corrupt: `block@${off - 3}` }
      const payload = btype === 0x01 ? 1 : bsize
      if (buffer.length - off < payload) return { frames, tornStart: start }
      off += payload
      if (last) break
    }
    if (checksum) {
      if (buffer.length - off < 4) return { frames, tornStart: start }
      off += 4
    }
    frames.push({ start, end: off })
  }
  return { frames }
}

/** Распаковать .jsonl.zstd в строку (только структурно полные фреймы). */
function decodeSession(bytes) {
  const { frames, tornStart } = scanZstdFrames(bytes)
  const parts = []
  let skipped = 0
  for (const f of frames) {
    try {
      parts.push(zstdDecompressSync(bytes.subarray(f.start, f.end)).toString('utf8'))
    } catch (e) {
      skipped += 1
    }
  }
  // const base = f => f / 1024 / 1024
  if (tornStart !== undefined) skipped += 1 // битый/незавершённый хвост (живая сессия)
  return { text: parts.join(''), frames: frames.length, skipped }
}

/* ------------------------------ usage aggregation ------------------------ */
function addUsage(agg, u, timeMs) {
  if (!u) return
  agg.input += u.inputTokens ?? 0
  agg.cacheRead += u.cacheReadTokens ?? 0
  agg.cacheWrite += u.cacheWriteTokens ?? 0
  agg.output += u.outputTokens ?? 0
  agg.reasoning += u.reasoningTokens ?? 0
  const off = timeMs === undefined || isOffPeak(timeMs)
  const c = usageCost(u, off ? OFFPEAK : PEAK)
  agg.cost += c
  if (off) { agg.costOffpeak += c; agg.offpeakEvents += 1 } else { agg.costPeak += c; agg.peakEvents += 1 }
}

const emptyAgg = () => ({ input: 0, cacheRead: 0, cacheWrite: 0, output: 0, reasoning: 0, events: 0, cost: 0, costOffpeak: 0, costPeak: 0, offpeakEvents: 0, peakEvents: 0 })

function parseSession(file) {
  const bytes = readFileSync(file)
  const { text, frames, skipped } = decodeSession(bytes)
  const agg = emptyAgg()
  if (!text) return { agg, frames, skipped, error: null }
  let lines = 0
  for (const line of text.split('\n')) {
    const t = line.trim()
    if (!t) continue
    lines += 1
    let ev
    try { ev = JSON.parse(t) } catch { continue }
    if (ev.type === 'assistant/chunk' && ev.data?.chunk?.type === 'usage') {
      addUsage(agg, ev.data.chunk.usage, ev.time)
      agg.events += 1
    } else if (ev.type === 'assistant/message' && ev.data?.message?.usage) {
      addUsage(agg, ev.data.message.usage, ev.time)
      agg.events += 1
    }
  }
  return { agg, frames, skipped, lines, error: null }
}

/* ------------------------------ session listing -------------------------- */
function findSessionFiles(root) {
  const out = []
  const walk = dir => {
    let entries
    try { entries = readdirSync(dir, { withFileTypes: true }) } catch { return }
    for (const e of entries) {
      const p = join(dir, e.name)
      if (e.isDirectory()) walk(p)
      else if (e.name.endsWith('.jsonl.zstd')) out.push(p)
    }
  }
  walk(root)
  return out
}

/* ------------------------------ credentials ------------------------------ */
function readApiKey() {
  try {
    const raw = readFileSync(CREDENTIALS, 'utf8')
    for (const line of raw.split('\n')) {
      const m = line.match(/^\s*DEEPSEEK_API_KEY\s*:\s*(.+)$/)
      if (!m) continue
      let v = m[1].trim()
      // убрать комментарий после значения (аккуратно, не трогая сам ключ)
      v = v.replace(/[ \t]+#.*$/, '').trim()
      if ((v.startsWith('"') && v.endsWith('"')) || (v.startsWith("'") && v.endsWith("'"))) v = v.slice(1, -1)
      return v || null
    }
  } catch { /* ignore */ }
  return null
}

/* ------------------------------ balance ---------------------------------- */
async function fetchBalance(key) {
  if (!key) return { ok: false, error: 'нет ключа DEEPSEEK_API_KEY в .credentials.yaml' }
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 15000)
  try {
    const res = await fetch(BALANCE_URL, {
      headers: { Authorization: `Bearer ${key}`, Accept: 'application/json' },
      signal: controller.signal,
    })
    if (!res.ok) return { ok: false, error: `HTTP ${res.status}` }
    const j = await res.json()
    const infos = Array.isArray(j.balance_infos) ? j.balance_infos : []
    const usd = infos.find(i => i.currency === 'USD')
    const cny = infos.find(i => i.currency === 'CNY')
    return { ok: true, isAvailable: j.is_available, usd, cny, raw: infos }
  } catch (e) {
    return { ok: false, error: e.name === 'AbortError' ? 'таймаут запроса' : e.message }
  } finally {
    clearTimeout(timer)
  }
}

/* ------------------------------ formatting ------------------------------- */
const usd = v => '$' + (v ?? 0).toFixed(4)
const num = v => (v ?? 0).toLocaleString('en-US')

function printTokensHeader() {
  console.log(`Цены ($/1M токенов) | off-peak: вход=${OFFPEAK.input} кэш-чтение=${OFFPEAK.cacheRead} кэш-запись=${OFFPEAK.cacheWrite} выход=${OFFPEAK.output}`)
  console.log(`                     | peak:    вход=${PEAK.input} кэш-чтение=${PEAK.cacheRead} кэш-запись=${PEAK.cacheWrite} выход=${PEAK.output}`)
  console.log(`Peak/off-peak по времени запроса: TZ=${TZ}, off-peak ${fmtHM(OFFPEAK_START)}–${fmtHM(OFFPEAK_END)}${OFFPEAK_WEEKEND ? ', выходные — весь день off-peak' : ''} (reasoning включён в выход)`)
  console.log('Проверьте актуальные цены: https://api-docs.deepseek.com/quick_start/pricing\n')
}

async function main() {
  const args = process.argv.slice(2)
  const wantTokens = !args.includes('--balance') || args.includes('--tokens')
  const wantBalance = !args.includes('--tokens') || args.includes('--balance')

  if (wantTokens) {
    const files = findSessionFiles(SESSIONS_ROOT)
    if (files.length === 0) {
      console.log(`Сессий не найдено в ${SESSIONS_ROOT}`)
    } else {
      const grand = emptyAgg()
      console.log(`Файлов .jsonl.zstd: ${files.length}  (корень: ${SESSIONS_ROOT})\n`)
      printTokensHeader()
      const rows = files.map(file => {
        const { agg, frames, skipped } = parseSession(file)
        for (const k of Object.keys(grand)) grand[k] += agg[k]
        // группировка: sessions/<workspace>/<id>/session.jsonl.zstd
        const rel = relative(SESSIONS_ROOT, file)
        const parts = rel.split(/[\\/]/)
        const workspace = parts.length >= 2 ? parts[0] : '(корень)'
        const sessionDir = parts.length >= 2 ? parts[1] : basename(dirname(file))
        return { workspace, sessionDir, agg, frames, skipped, cost: agg.cost }
      })
      const w = 42
      const line = `${'SESSION'.padEnd(w)} | in | cacheR | cacheW | out | reason | USD`
      console.log(line)
      console.log('-'.repeat(line.length))
      const byWs = {}
      for (const r of rows) {
        console.log(`${(r.sessionDir).padEnd(w)} | ${num(r.agg.input).padStart(7)} | ${num(r.agg.cacheRead).padStart(6)} | ${num(r.agg.cacheWrite).padStart(6)} | ${num(r.agg.output).padStart(7)} | ${num(r.agg.reasoning).padStart(6)} | ${usd(r.cost)}  (${r.workspace})`)
        const b = byWs[r.workspace] || (byWs[r.workspace] = emptyAgg())
        for (const k of Object.keys(b)) b[k] += r.agg[k]
      }
      console.log('-'.repeat(line.length))
      console.log(`${'ИТОГО'.padEnd(w)} | ${num(grand.input).padStart(7)} | ${num(grand.cacheRead).padStart(6)} | ${num(grand.cacheWrite).padStart(6)} | ${num(grand.output).padStart(7)} | ${num(grand.reasoning).padStart(6)} | ${usd(grand.cost)}`)
      console.log('\nСуммарно затрачено (оценка): ' + usd(grand.cost))
      console.log(`  off-peak: ${usd(grand.costOffpeak)} (${grand.offpeakEvents} событий) · peak: ${usd(grand.costPeak)} (${grand.peakEvents} событий)`)
    }
  }

  if (wantBalance) {
    if (wantTokens) console.log('')
    const key = readApiKey()
    const b = await fetchBalance(key)
    if (!b.ok) {
      console.log(`Баланс счёта: недоступен (${b.error})`)
    } else {
      console.log(`Баланс счёта DeepSeek (is_available=${b.isAvailable}):`)
      if (b.usd) console.log(`  USD: ${usd(Number(b.usd.total_balance))}  (granted=${usd(Number(b.usd.granted_balance))}, topped_up=${usd(Number(b.usd.topped_up_balance))})`)
      if (b.cny) console.log(`  CNY: ¥${(Number(b.cny.total_balance)).toFixed(2)}  (granted=¥${(Number(b.cny.granted_balance)).toFixed(2)}, topped_up=¥${(Number(b.cny.topped_up_balance)).toFixed(2)})`)
      if (!b.usd && !b.cny) console.log(`  (валюта не найдена: ${JSON.stringify(b.raw)})`)
    }
  }
}

await main()
