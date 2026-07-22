# MMI-Widgets

An Android app for sending **MMI codes** — the `*` / `#` supplementary-service sequences you
normally type into the dialer — to quickly toggle telephony functions such as **call forwarding**,
without dialing them by hand every time.

> [!WARNING]
> **This project is AI-assisted.** 
> 
> No warranty of any kind; use at your own risk.

## What it does

1. **Send MMI codes** to toggle telephony functions (call forwarding is the worked example).
2. **Persist settings** — a forwarding number and the last known on/off state of each function.
3. **Home-screen widgets** to toggle a function with a single tap, without opening the app.

MMI / supplementary-service codes are executed by *dialing* them. The app builds a `tel:` URI and
fires an `ACTION_CALL` intent (requiring the `CALL_PHONE` runtime permission). There is no reliable
success callback, so stored on/off state is **optimistic**: the app assumes a toggle worked and
flips the flag. If real and stored state drift, re-toggle.

The default codes are standard GSM sequences (`**21*<n>#` / `##21#`, etc.). **Carriers vary** —
treat them as defaults, not guarantees.

## Requirements

| | |
|---|---|
| Language | **Java only** (classic Views + ViewBinding — no Kotlin, no Jetpack Compose) |
| Application id | `net.buechsenschuss.mmiwidgets` |
| `minSdk` | 35 |
| `targetSdk` / `compileSdk` | 36 |
| JDK | Java 11 (the Android Studio JBR works — see below) |

## Project layout

```
app/src/main/java/net/buechsenschuss/mmiwidgets/
  MainActivity.java             Config screen: save number, toggle functions.
  MmiDispatchActivity.java      Invisible activity: resolves + dials a code, handles CALL_PHONE
                                permission, updates state, refreshes widgets. Single choke point.
  model/MmiAction.java          Data model for one toggleable function.
  data/MmiActions.java          Hard-coded registry of known functions. Add new toggles here.
  data/SettingsStore.java       SharedPreferences wrapper (number + per-action on/off state).
  mmi/MmiSender.java            Builds the tel: URI and fires ACTION_CALL.
  widget/MmiWidgetProvider.java Home-screen widget (AppWidgetProvider).

app/src/main/res/               Layouts, launcher icon, strings/colors/themes.
app/src/test/java/...           Pure-JVM unit tests (run without a device).
```

## Build & run

`JAVA_HOME` must be set first — this repo assumes no JDK on `PATH`. The JBR bundled with Android
Studio works:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

Then, from the repo root (`gradlew.bat` on Windows, `./gradlew` on Unix):

```
gradlew.bat test                              # fast: pure-JVM unit tests, no device needed
gradlew.bat :app:compileDebugJavaWithJavac    # compile check
gradlew.bat assembleDebug                      # build the APK
gradlew.bat installDebug                       # install on a connected device/emulator
```

MMI dialing, permissions, and widgets do nothing on a stock emulator — verify them on a **real
device with a SIM**.

## Launcher icon

The adaptive launcher icon is built from the app's `*` / `#` dial-code motif and ships as two
switchable concepts (`res/drawable/ic_launcher_{split,prompt}_*`):

- **Split tile** (default) — a two-tone dark/electric-blue split with a neon `*` and a dark `#`.
- **Prompt** — a `>*#` terminal cue in neon green on a dark radial-glow tile.

To switch, replace `split` with `prompt` in the three drawable references in both
`res/mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml`.

## Contributing / agents

Agent and contributor instructions live in **[AGENTS.md](./AGENTS.md)** — read it before making
changes. It covers the Java-only / no-Compose constraint, the dispatch architecture, how to extend
the app, and the full build/test commands.
