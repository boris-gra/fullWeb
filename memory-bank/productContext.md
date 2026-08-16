# Product Context — fullWeb (Query)

## Зачем существует
- Универсальный инструмент для работы с представлениями баз данных (PostgreSQL, Google BigQuery) без написания кода.
- Девиз: «Look and modify ANY view from ANY base».

## Как работает
- Источники данных настраиваются переменными окружения (см. projectbrief.md).
- Описание доступных запросов находится в представлении QUERY_BD (меню Construction → Development → Queries).

## UX-цели
- Единый интерфейс для разных источников данных.
- Развёртывание в облаке (Koyeb, Firebase/Cloud Run) и в Docker.
