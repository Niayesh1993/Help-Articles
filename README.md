# Help Articles App

## Architecture & Key Decisions

**MVVM + Clean Architecture (lite)**
Each screen owns a ViewModel that exposes a single `StateFlow<UiState>` sealed
class. Screens observe state via `collectAsStateWithLifecycle` and render
accordingly. No intermediate "domain layer" was added — the project scope doesn't
justify it within the time box.

**Dependency Injection**: Hilt — minimal boilerplate, first-class ViewModel support.

**Navigation**: Jetpack Navigation Compose with type-safe argument passing via
`navArgument`.

---

## Network vs. Backend Errors

Two distinct error types flow through the entire stack:

| Type | When | UI |
|---|---|---|
| `AppError.ConnectivityError` | IOException, timeout, HTTP 5xx, malformed JSON | Cloud-off icon, neutral tone, "Retry" button |
| `AppError.BackendError` | HTTP 200 with `{ "error": { errorCode, errorTitle, errorMessage } }` | Error-container card, server text surfaced verbatim |

The repository is the single place that translates `Exception` subtypes
(`ConnectivityException`, `BackendException`) into `AppError`. ViewModels
and Compose screens never touch raw exceptions.

---

## Auto-refresh & Background Prefetch

**Auto-refresh**: On every `loadArticles()`, the repository checks
cache freshness first. If stale or absent, it fetches. On connectivity
failure, it falls back to stale cache (if any), so the screen always shows
*something* when possible.

**Background prefetch**: `WorkManager` `PeriodicWorkRequest` with a 1-day
interval and 2-hour flex window.
- Constraints: `CONNECTED` + `BATTERY_NOT_LOW`
- Policy: `KEEP` — prevents duplicate enqueue on process restart
- Backoff: exponential from 10 min, max 3 retries

WorkManager was chosen over AlarmManager/JobScheduler because it is
battery-aware, respects Doze, survives reboots without extra BroadcastReceivers,
and handles retries automatically.

---

## Staleness / Expiry Rule

| Cache | TTL | Rationale |
|---|---|---|
| Article list | 15 minutes | Reflects editorial changes promptly; short enough to surface new articles on app resume |
| Article detail | 30 minutes | Content changes less frequently; longer TTL reduces redundant fetches |

On cache miss (stale or absent), the app fetches live. On network failure,
`getArticleListIgnoringTtl()` / `getArticleDetailIgnoringTtl()` return
whatever is stored regardless of age so the user always sees *some* content.

The KMP `ArticleCache` lives in `:shared`, backed by a `CacheStorage`
interface. On Android, `SharedPreferencesCacheStorage` satisfies the interface,
giving persistence across process deaths. In tests, `InMemoryCacheStorage`
is used for fast, deterministic behaviour.

---

## What's Completed vs. Skipped

### Completed
- Article list screen with search/filter
- Article detail screen with Markwon Markdown rendering
- Connectivity error UX + backend error UX, fully distinct
- KMP cache module with TTL/staleness + SharedPreferences Android backend
- Offline fallback (stale cache served on IOException)
- Daily background prefetch with WorkManager
- Light/dark theme (Material 3, dynamic color on Android 12+)
- Accessible touch targets, scalable text, semantic content descriptions
- MockInterceptor covering list, detail, backend error, and ~20% transport errors
- 4 KMP unit tests (list freshness, staleness, boundary, detail scoping)
- 2 Compose UI tests (connectivity error + backend error state + Retry)

### Skipped (time-box trade-offs)
- Pull-to-refresh gesture (refresh icon button used instead)
- Connectivity-change listener for automatic re-fetch when network returns
  (would use `ConnectivityManager.NetworkCallback` + a SharedFlow in the repo)
- Paging (not needed at current mock data scale)
- iOS KMP target (explicitly excluded by requirements)

