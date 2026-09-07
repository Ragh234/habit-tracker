# Habit Tracker

An offline Android habit tracker. No backend, no network calls — everything lives in a
local Room database and DataStore. Built as a second Kotlin/Compose project to sit
alongside a Retrofit-backed app on the same resume, deliberately covering the pieces that
one doesn't: WorkManager, DataStore, Room migrations, and Compose Navigation.

## What it does

- **Home** — list of habits, each with its current streak and a done-today checkbox.
- **Detail** — a habit's last 30 days as a grid, edit its name/target/colour, delete it.
- **Settings** — a daily reminder time, backed by a background job that checks whether
  anything is still unchecked and fires a local notification if so.

## Why it's built this way

**The unique index on `(habitId, date)`.** Checking off a habit is an insert, not an
insert-after-checking-if-it-exists. The `Index(value = ["habitId", "date"], unique = true)`
on `check_ins` means a duplicate insert for the same habit on the same day is rejected by
SQLite itself, and `OnConflictStrategy.IGNORE` turns that rejection into a silent no-op.
A double tap or a retried background job can't produce two rows. See
[`CheckInEntity.kt`](app/src/main/java/com/example/habittracker/data/local/CheckInEntity.kt).

**A real Room migration.** The `habits` table shipped in schema version 1 without a colour
column. Version 2 adds `colorHex` via an actual `ALTER TABLE`, not a fresh-install-only
change — see [`Migrations.kt`](app/src/main/java/com/example/habittracker/data/local/Migrations.kt).
It's covered by an instrumented `MigrationTestHelper` test that builds a real v1 database,
inserts a row, runs the migration, and checks the row survived with the right default. Room
also compares the migrated schema against the exported `2.json` automatically, which is
what caught a bug during development — see BUGS.md #2.

**WorkManager, not AlarmManager.** The daily reminder is a `PeriodicWorkRequest` enqueued
with `enqueueUniquePeriodicWork` under a stable name, so re-saving the same settings can't
stack duplicate jobs. It survives process death and reboot, retries with exponential
backoff up to 3 times, and — because the reminder time can change after it's already
scheduled — the app has to choose between `UPDATE` (keep the pending run, swap the
request) and `CANCEL_AND_REENQUEUE` (drop the pending run, start fresh) depending on
whether the *time itself* changed. Getting that wrong was BUGS.md #3.

**DataStore over SharedPreferences.** Flow-based, async by default, and it reports read
failures through the Flow instead of throwing at the call site — `.catch { IOException }`
in [`SettingsRepository.kt`](app/src/main/java/com/example/habittracker/data/prefs/SettingsRepository.kt)
is what stops a corrupt preferences file from crashing the app at launch.

**`StreakCalculator` is pure Kotlin.** No `Context`, no Room, no Android import at all —
just `List<LocalDate>` in, `Int` out. That's what makes it unit-testable on the JVM
without an emulator; see the 8 cases in
[`StreakCalculatorTest.kt`](app/src/test/java/com/example/habittracker/domain/StreakCalculatorTest.kt).

## Stack

Kotlin, Jetpack Compose (Material 3), Hilt, Room + KSP, Compose Navigation, DataStore
Preferences, WorkManager, coroutines/Flow. AGP 9's built-in Kotlin support — no
`org.jetbrains.kotlin.android` plugin.

## Structure

```
data/
  local/       Room entities, DAO, database, migration
  prefs/       DataStore-backed settings
  HabitRepository.kt
domain/
  StreakCalculator.kt   pure Kotlin, unit-tested
work/
  ReminderWorker.kt, ReminderScheduler.kt
notification/
  Notifier.kt
di/
  AppModule.kt          Hilt bindings
ui/
  home/, detail/, settings/   one screen + one ViewModel each
  NavGraph.kt
```

## Running it

```
./gradlew assembleDebug
```

Needs `compileSdk`/`targetSdk` 37 and a JDK matching Gradle 9.5 (JDK 25 is what this was
built and tested with).

## What I'd add next

- Weekly/monthly stats instead of just the current streak.
- Widget support — a home-screen widget is the natural next WorkManager-adjacent feature.
- Export/import so a reinstall doesn't lose history, since there's no backend to fall
  back on.

## Bugs hit while building this

See [BUGS.md](BUGS.md) — three real ones: an AGP 9 / KSP configuration conflict, a Room
migration whose exported schema disagreed with itself over a default value, and a
WorkManager rescheduling bug where changing the reminder time didn't move the reminder
until the following day.
