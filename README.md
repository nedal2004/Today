# 📱 Today — Focus Widget Task App

> A 100% offline Android productivity app built around a **home screen widget** that shows exactly one thing: the task that matters right now.

---

## 🎯 The Idea

Most task apps treat the widget as an afterthought — a shrunken version of the app's list view, crammed onto a home screen. **Today inverts that**: the widget *is* the product. It shows one focus task and a count of what's left, so checking your day never requires opening the app at all.

No accounts. No servers. No paid APIs. Everything lives in a local Room database — which also made this a natural fit for offline-first use cases.

---

## 📲 Screenshots

> *(To be added — first working screenshots of the task list and the home screen widget)*

| Task List | Focus Widget |
|---|---|
| _coming soon_ | _coming soon_ |

---

## 🏗️ Architecture

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│   UI Layer  │◀────▶│  ViewModel   │◀────▶│ Repository  │
│ Compose +   │ Flow │ (state +     │      │ (single     │
│ Glance      │      │  events)     │      │  source of  │
└─────────────┘      └──────────────┘      │  truth)     │
                                            └──────┬──────┘
                                                   │
                                            ┌──────▼──────┐
                                            │  Room (DB)  │
                                            └─────────────┘
```

**MVVM + Repository**, with Room as the single source of truth feeding both the app screen and the widget. Neither the UI nor the widget talks to the database directly — everything routes through `TaskRepository`, which is what keeps the two surfaces consistent without any manual sync logic.

**Patterns applied — deliberately, not by default:**

| Pattern | Where | Why it earned its place |
|---|---|---|
| MVVM | App-wide | Clean separation between UI and logic |
| Repository | ViewModel ↔ Room | One source of truth for app + widget |
| Observer (`Flow`) | Room → UI | UI reacts to data changes automatically |
| Singleton | `AppDatabase` | One database instance, app-wide |
| Manual DI | `TodayApp` → Repository | Simple enough that Hilt would be overkill |

A few patterns were deliberately **left out**. The Undo feature, for example, is a single cached `Task` plus two functions — not a full Command/Memento implementation — because the actual requirement (undo the last completion) didn't justify the extra machinery. That's a documented decision, not a shortcut: see [Engineering Decisions](#-engineering-decisions--why-things-are-built-this-way) below.

---

## ✅ What's Built So Far

- [x] **Room data layer** — `Task` entity, DAO with hand-written SQL queries, `Bucket` enum (TODAY / TOMORROW / LATER)
- [x] **Task list UI** — Jetpack Compose, tabbed buckets, add/edit/delete
- [x] **Swipe-to-complete + Undo** — Snackbar-based undo, empty states per bucket, Arabic localization
- [x] **Daily rollover** — WorkManager + startup check hybrid; TOMORROW tasks promote to TODAY automatically
- [x] **Focus Widget (Jetpack Glance)** — home screen widget showing the top task + a count of the rest

**In progress:**
- [ ] Widget ↔ app live sync (`updateAll()` after data changes)
- [ ] UML diagrams, final polish, portfolio packaging

---

## 🧠 Engineering Decisions — why things are built this way

This section exists because *how* a bug got fixed is often more useful to show than the final diff. A few of the real ones from this build:

### The widget got stuck on a permanent loading screen — three unrelated causes stacked together

The Focus Widget appeared in the widget picker but never rendered — just an infinite loading spinner. Debugging it top-to-bottom surfaced three separate, unrelated problems:

1. **A Restricted API silently accepted, then flagged.** `ColorProvider(resId)` compiled fine but is marked `@RestrictedApi` by the Glance team — because color resources can resolve differently in the launcher's process vs. the app's process. Fix: use the public `ColorProvider(day = Color(...), night = Color(...))` overload instead of suppressing the lint warning.
2. **`namespace` and `applicationId` didn't match the actual package structure.** Gradle had `com.nedal.today` while every Kotlin file lived under `com.n.alian.today`. The Activity still launched (fully-qualified manifest names covered for it), but the widget provider — which resolves more strictly — failed silently. Fixed by aligning `namespace`/`applicationId` to match the real code, followed by a full uninstall (Android won't "update" across an applicationId change).
3. **A missing `android:initialLayout`.** Without a loading layout declared in `today_widget_info.xml`, some launchers (MIUI included) reject the `AppWidgetProviderInfo` outright rather than rendering a fallback.

None of these three would have been caught by looking at any single file in isolation — each one produced a *different* symptom, and they had to be resolved one at a time to know which fix mapped to which failure.

### Undo is a cached value, not a Command pattern — and that's a design choice, not a gap

The book/course material this project is built alongside spends real time on the Command + Memento pattern for undo. It's not used here. The actual requirement — "undo the last task completion" — is fully satisfied by:

```kotlin
private var lastCompletedTask: Task? = null

fun onComplete(task: Task) {
    lastCompletedTask = task  // cache the pre-mutation copy
    viewModelScope.launch(Dispatchers.IO) {
        repository.update(task.copy(isDone = true, completedAt = System.currentTimeMillis()))
    }
}

fun onUndo() {
    val task = lastCompletedTask ?: return
    viewModelScope.launch(Dispatchers.IO) { repository.update(task) }
    lastCompletedTask = null
}
```

If multi-step undo ever becomes a real requirement, this is a known, documented upgrade path to Command pattern — not a rewrite. Building the full pattern now, for a single-step undo, would be solving a problem the app doesn't have yet.

### The daily rollover doesn't rely on `PeriodicWorkRequest` alone

`WorkManager`'s periodic jobs drift under Doze mode — they're not guaranteed to fire at a precise time. Since bucket correctness (TOMORROW → TODAY at midnight) needs to be *right* whenever the user opens the app, the actual guarantee comes from a fast `SharedPreferences` check on `Application.onCreate()` (has today's rollover already run?), with the 24-hour periodic worker kept only as a backup for the case where the app isn't opened for days but the widget is still being viewed.

### Glance widgets don't auto-update — and that's expected, not a bug

`provideGlance()` runs once, when the widget is added or explicitly told to refresh. It is **not** a persistent `Flow` subscription the way Compose UI is. That means deleting a task in the app doesn't refresh the widget on its own — the fix is an explicit `TodayWidget().updateAll(context)` call after any repository mutation, which is what the current round of work is adding.

---

## 🛠️ Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| App UI | Jetpack Compose |
| Widget UI | Jetpack Glance |
| Database | Room 2.7.1 |
| Background work | WorkManager |
| Async | Kotlin Coroutines + Flow |
| Architecture | MVVM + Repository |
| Connectivity | 100% offline |

---

## 🚀 Setup

```bash
git clone https://github.com/nedal2004/today.git
cd today
```

Open in Android Studio, sync Gradle, and run. No API keys, no backend, no configuration — the app works fully offline out of the box.

---

## 📍 Roadmap

- Widget interactivity — complete/add tasks directly from the widget
- Material You dynamic color
- Morning summary notification
- Daily streak tracking
- Weekly stats screen (all computable locally from Room — no new infrastructure needed)

---

## 👤 Author

**Nedal Alian** — Final-year Mobile Computing student.
Built as a portfolio piece and as the technical foundation for a graduation project extension (context-aware task suggestions via geofencing + calendar signals).

[GitHub](https://github.com/nedal2004)
