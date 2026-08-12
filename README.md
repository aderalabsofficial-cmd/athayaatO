# Atahyaat — Offline Prayer Times & Reminders

A fully offline Android prayer-time app: local astronomical calculation (no
internet required), exact alarms that ring even with the screen off, custom
sounds/vibration per prayer, dark mode, and a daily streak tracker.

## What's included

```
atahyaat/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/atahyaat/app/       ← all Kotlin source
│       └── res/                         ← layouts, icons, sounds, strings
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .github/workflows/build-apk.yml      ← builds the APK automatically
└── README.md
```

## Features implemented

- **Offline prayer times** — Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha computed
  locally from latitude/longitude/timezone using standard solar-position
  astronomy (Julian date, solar declination, equation of time). No network
  calls, ever.
- **20 preset cities** for quick offline location selection, plus 5
  calculation methods (MWL, ISNA, Egypt, Karachi, Umm al-Qura) and Asr method
  (Standard / Hanafi).
- **Alarms that ring with the screen off** — `AlarmManager.setExactAndAllowWhileIdle`
  + a foreground service + a full-screen activity that wakes the device,
  exactly like a real alarm clock.
- **Per-prayer alert customization** — choose Alarm / Notification /
  Vibration-only / Silent, and one of four subtle synthesized tones (Soft
  Chime, Gentle Bell, Soft Ping, Classic Tone) independently for each of the
  5 daily prayers.
- **Dark mode** toggle (Settings tab), backed by `AppCompatDelegate`.
- **Streak tracker** — mark each obligatory prayer as prayed; current streak,
  best streak, and a 7-day view, stored locally in a Room database.
- **Tasbih counter** on its own tab.
- **Reboot-safe** — a `BOOT_COMPLETED` receiver reschedules all alarms after
  the phone restarts, so nothing depends on the app staying open.
- Lightweight: no ads, no analytics, no network permissions beyond what's
  needed for GPS-based location (optional; preset cities work without it).

## Build the APK via GitHub (no Android Studio needed)

1. Create a new **public or private GitHub repository**.
2. Upload every file/folder from this project keeping the same structure
   (drag-and-drop the whole `atahyaat` folder contents into the repo, or use
   `git add . && git commit -m "Atahyaat" && git push`).
3. Once pushed to the `main` branch, GitHub Actions will automatically run
   the workflow in `.github/workflows/build-apk.yml`.
4. Go to the **Actions** tab of your repo → open the latest "Build Atahyaat
   APK" run → scroll to **Artifacts** → download `atahyaat-debug-apk.zip`.
   Inside is `app-debug.apk`, ready to install on any Android phone
   (Settings → allow installs from this source, then open the APK).
5. If the workflow doesn't start automatically, open the **Actions** tab and
   click **Run workflow** manually (the workflow also supports
   `workflow_dispatch`).

This produces a **debug** build, which is fine for personal installs and
testing. If you want a signed **release** build (for the Play Store), you'll
need to add a signing key and a `release` build step — happy to help with
that when you're ready to publish.

## Notification sounds

Four short, synthesized chime/bell tones are included in
`app/src/main/res/raw/` as placeholders (`tone_chime.wav`, `tone_bell.wav`,
`tone_soft.wav`, `tone_classic.wav`). If you have rights to real Adhan/Athan
audio you'd like to use instead, drop `.wav` or `.mp3` files into that same
`res/raw/` folder (lowercase, no spaces, e.g. `tone_adhan_makkah.wav`) and
reference the new filename in `SettingsFragment.kt`'s `soundOptions` list.

## App icon

The app icon you provided is already wired in as an adaptive icon
(`res/mipmap-*/ic_launcher*.png` + `res/mipmap-anydpi-v26/ic_launcher.xml`).

## Notes / next steps

- Location currently comes from a preset city list (offline-friendly) with
  the plumbing in place to add GPS-based auto-detect later if you want it.
- The debug APK is unsigned for the Play Store — that only matters if/when
  you publish; sideloading works fine as-is.
