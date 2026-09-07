# Bugs

Running log of things that broke while building this, what caused them, and what fixed
them.

---

## 1. Gradle sync failed the moment KSP was added

**Symptom**

Adding the KSP and Hilt plugins made configuration fail before any code compiled:

```
Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin.
Kotlin source set 'debug' contains: [app/build/generated/ksp/debug/kotlin, .../java]
```

**Cause**

AGP 9 applies Kotlin itself instead of requiring the `org.jetbrains.kotlin.android`
plugin. Under built-in Kotlin, AGP owns the source sets and rejects anything registering
extra ones through the old `kotlin.sourceSets` DSL. The KSP plugin still registers its
generated output directories that way, so the two disagree on who configures sources.

Nothing to do with Room or Hilt specifically — any KSP processor would have hit it.

**Fix**

`android.disallowKotlinSourceSets=false` in `gradle.properties`, which is Google's
documented escape hatch for exactly this case. The generated code still compiles; the
flag only re-allows the older way of pointing at it.

The alternative was to apply `org.jetbrains.kotlin.android` explicitly, which turns
built-in Kotlin off and puts the project back on the setup most docs assume. Kept
built-in Kotlin because that is what the project was generated with, and the flag is one
line against a whole plugin's worth of configuration.

---

## 2. The migration test would have failed on a default value

**Symptom**

After adding `colorHex` in schema version 2, the exported `2.json` recorded the column as
`TEXT NOT NULL` with `defaultValue: null`, while `MIGRATION_1_2` created it with
`DEFAULT '#4CAF50'`.

**Cause**

A freshly installed app builds its schema from the entity class. An upgraded app builds it
from the `ALTER TABLE` in the migration. Those are two different code paths that have to
produce an identical table, and `runMigrationsAndValidate` compares them column by column,
default values included. The Kotlin default (`val colorHex: String = DEFAULT_COLOR_HEX`) is
only a default for the constructor. It says nothing to SQLite.

So a new install would have had no SQL default and an upgraded install would have had one.

**Fix**

`@ColumnInfo(defaultValue = "#4CAF50")` on the field, so the entity declares the same SQL
default the migration writes. After that `2.json` records `'#4CAF50'` and the two paths
agree.

Caught by reading the exported schema rather than by the test, because the migration test
is instrumented and needs a device.

---

## 3. Changing the reminder time did not move the reminder

**Symptom**

Set the reminder to 20:00, then changed it to 08:00. The 20:00 run still fired. The new
time only took effect a cycle later.

**Cause**

`enqueueUniquePeriodicWork` was using `ExistingPeriodicWorkPolicy.UPDATE` for every save.
UPDATE keeps the work that is already scheduled and swaps the new request in underneath
it. The pending run had already been scheduled against the old initial delay, and UPDATE
does not move it, so the schedule stayed on the old time until that run completed and the
next period was computed.

UPDATE is still the right policy when nothing about the timing changed. The mistake was
using it unconditionally.

**Fix**

The settings screen now compares the new time against what was already stored and picks
the policy from that: `CANCEL_AND_REENQUEUE` when the time changed or the reminder was
just switched on, `UPDATE` otherwise. Cancelling drops the stale pending run so the new
initial delay is the one that counts.

**Still not verified on a device.** The unique name means this cannot stack duplicate
workers either way, so the failure is silent: the reminder fires, just at the old time.
Check it with `adb shell dumpsys jobscheduler` and look at the scheduled run time before
and after changing the setting.
