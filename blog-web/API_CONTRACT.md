# Frontend API contract

The frontend defaults to `VITE_DATA_MODE=mock`. Set `VITE_DATA_MODE=api` and
`VITE_API_BASE_URL=/api` when the Java service is available.

## Endpoints

- `GET /site/home`
- `GET /categories`
- `GET /articles?category=&year=&keyword=&page=&pageSize=`
- `GET /articles/{id}`
- `GET /archive`
- `GET /site/about`

`VITE_API_BASE_URL` is prepended to every endpoint.

## Article summary

```json
{
  "id": "0",
  "title": "Article title",
  "summary": "Short summary",
  "category": "travel",
  "categoryLabel": "旅行游记",
  "coverUrl": "https://cdn.example.com/cover.jpg",
  "publishedAt": "2026-07-04",
  "views": 91
}
```

Article detail adds `contentHtml`, `readMinutes`, `originalUrl`, `previous`, and
`next`. Pagination responses use `{ items, total, page, pageSize, hasMore }`.
All IDs are strings. Dates use ISO `YYYY-MM-DD` values.

Archive responses are grouped as years and months. Month groups deliberately use
the same `count` and `items` field names as the mock repository:

```json
[
  {
    "year": 2026,
    "count": 24,
    "months": [
      { "month": 7, "label": "七月", "count": 2, "items": [] }
    ]
  }
]
```

## Unified response

API mode uses a unified envelope. The repository automatically unwraps `data`, so
page components continue receiving the structures documented above.

```json
{
  "code": "20000",
  "message": "ok",
  "data": {}
}
```
