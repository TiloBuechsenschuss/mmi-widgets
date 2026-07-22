# Agent instructions — MMI-Widgets

This file is the source of truth for anyone (human or AI agent) working in this repository.
`CLAUDE.md` intentionally just points here, and `README.md` is the user-facing overview — keep all
three consistent when you change how the app is built, run, or structured.

> **This is an AI-generated / AI-assisted project.** Most code and docs here were produced by an AI
> agent and have not been audited for production use. Keep the AI warning in `README.md` intact, and
> do not remove or soften it without the user explicitly asking.

## What this app is

An Android app that sends **MMI codes** (the `*`, `#` supplementary-service sequences you normally
type into the dialer) to quickly turn telephony functions like **call forwarding** on and off.

Core capabilities:

1. **Send MMI codes** to toggle functions (call forwarding is the worked example).
2. **Persist settings** — a forwarding number and the last known on/off state of each function.
3. **Home-screen widgets** to toggle a function with a single tap, without opening the app.

## Hard constraints

- **Language: Java only.** No Kotlin. The original Android Studio template was Kotlin/Compose and
  has been fully converted to Java + classic Android Views. Do not reintroduce Kotlin or Jetpack
  Compose (Compose is Kotlin-only). UI is XML layouts + `ViewBinding`.
- **Minimum surface area.** This is a skeleton meant to be tested and improved iteratively. Prefer
  small, verifiable changes over large rewrites.
- **Git is read-only for agents.** You may run read-only git commands (`status`, `log`, `diff`).
  Never stage, commit, amend, push, or otherwise modify the git tree unless the user explicitly
  asks in that moment.

## Project layout

```
app/src/main/java/net/buechsenschuss/mmiwidgets/
  MainActivity.java            Config screen: save number, toggle functions.
  MmiDispatchActivity.java     Invisible activity: resolves + dials a code, handles CALL_PHONE
                               permission, updates state, refreshes widgets. Single choke point.
  model/MmiAction.java         Data model for one toggleable function (enable/disable templates).
  data/MmiActions.java         Hard-coded registry of known functions. Add new toggles here.
  data/SettingsStore.java      SharedPreferences wrapper (number + per-action on/off state).
  mmi/MmiSender.java           Builds the tel: URI and fires ACTION_CALL.
  widget/MmiWidgetProvider.java Home-screen widget (AppWidgetProvider).

app/src/main/res/
  layout/activity_main.xml     Main screen.
  layout/item_action.xml       One dynamically-inflated action row.
  layout/widget_mmi.xml        Widget layout (RemoteViews — basic views only).
  xml/mmi_widget_info.xml       Widget metadata.
  drawable/widget_background.xml
  drawable/ic_launcher_{split,prompt}_{background,foreground}.xml  Adaptive launcher icon layers,
                               two switchable concepts (split = default). Switch by swapping the
                               drawable refs in mipmap-anydpi/ic_launcher{,_round}.xml.
  values/{strings,colors,themes}.xml

app/src/test/java/...          Pure-JVM unit tests (run without a device).
```

Build config: `app/build.gradle` (Groovy DSL, Java 11, `viewBinding`), dependency versions in
`gradle/libs.versions.toml`. `minSdk` is 35, `targetSdk`/`compileSdk` 36.

## How MMI sending works (important background)

- MMI / supplementary-service codes are executed by **dialing** them. We build a `tel:` URI with
  `Uri.fromParts("tel", code, null)` so `#` is percent-encoded correctly, then start
  `Intent.ACTION_CALL`. That intent requires the **`CALL_PHONE`** runtime permission.
- Everything routes through `MmiDispatchActivity` so permission handling, number substitution,
  state persistence, and widget refresh live in exactly one place. UI and widgets both build an
  intent via `MmiDispatchActivity.createIntent(...)`.
- Sending a code gives **no reliable success callback**. The stored on/off state is therefore
  *optimistic*: we assume the toggle worked and flip the flag. If real state and stored state
  drift, the user re-toggles. A future improvement is to query actual network state.
- The default codes in `MmiActions` are standard GSM codes (`**21*<n>#` / `##21#`, etc.). Carriers
  vary; treat them as defaults, not guarantees. A natural next step is letting users edit codes.

## Extending the app

- **Add a function:** add an `MmiAction` to `MmiActions.buildRegistry()`. It automatically appears
  as a row on the main screen (rows are generated from `MmiActions.all()`).
- **Per-widget function:** currently every widget controls `ID_CALL_FORWARDING`. To let each widget
  pick a function, add a widget configuration activity that stores the chosen action id keyed by
  `appWidgetId`, and read it in `MmiWidgetProvider.updateWidget`.

## Build & test

**`JAVA_HOME` must be set first.** This machine has no JDK on `PATH`; use the one bundled with
Android Studio at `C:\Program Files\Android\Android Studio\jbr`. Without it Gradle fails with
`ERROR: JAVA_HOME is not set and no 'java' command could be found`. In PowerShell:

```
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

Run from the repo root (Windows shell shown; use `./gradlew` on Unix):

```
gradlew.bat test                       # fast: pure-JVM unit tests, no device needed
gradlew.bat :app:compileDebugJavaWithJavac   # compile check
gradlew.bat assembleDebug              # build the APK
gradlew.bat installDebug               # install on a connected device/emulator
```

Prefer `test` and the compile check for fast iteration. Anything touching MMI dialing, permissions,
or widgets must be verified on a **real device with a SIM** — MMI codes do nothing on a stock
emulator.

## Conventions

- Match the existing style: `final` classes for utilities/registries, `@NonNull`/`@Nullable`
  annotations, Javadoc on public types explaining the *why*.
- Keep user-facing text in `res/values/strings.xml`, never hard-coded in Java.
- Keep the dispatch path single-sourced through `MmiDispatchActivity`.
```
