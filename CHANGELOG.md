2026-02-23 — Stabilize Android Instrumented CI Runner

- Switched `android-instrumented` GitHub Actions job from `macos-14` ARM emulator to `ubuntu-latest` + `x86_64` emulator for `reactivecircus/android-emulator-runner` stability.
- Added `disable-animations: true` to reduce UI test flakiness.
- Updated `README.md` CI section to reflect the new runner/emulator configuration.
