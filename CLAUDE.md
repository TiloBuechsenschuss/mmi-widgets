# CLAUDE.md

Agent instructions for this repository live in **[AGENTS.md](./AGENTS.md)**. Read it before making
changes — it covers the app's purpose, the Java-only / no-Compose constraint, the project layout,
how MMI sending works, how to extend the app, and the build/test commands.

Key reminders:

- **Java only**, classic Views + ViewBinding. No Kotlin, no Jetpack Compose.
- **Git is read-only** for agents — never modify the git tree unless the user explicitly asks.
- Fast iteration: `gradlew.bat test` and `gradlew.bat :app:compileDebugJavaWithJavac`.
- MMI dialing, permissions, and widgets need a **real device with a SIM** to verify.
