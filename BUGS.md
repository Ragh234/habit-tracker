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
