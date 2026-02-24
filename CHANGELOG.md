2026-02-23 — Stabilize Android Instrumented CI Runner

- Switched `android-instrumented` GitHub Actions job from `macos-14` ARM emulator to `ubuntu-latest` + `x86_64` emulator for `reactivecircus/android-emulator-runner` stability.
- Added `disable-animations: true` to reduce UI test flakiness.
- Updated `README.md` CI section to reflect the new runner/emulator configuration.
- Fixed Ubuntu AVD creation failure by replacing unsupported `profile: pixel_8` with `profile: pixel` (supported by `avdmanager` on CI image).
- Enabled `/dev/kvm` access on Ubuntu runner and added explicit emulator boot flags/timeout to prevent non-accelerated boot failures.
- Stabilized app instrumentation tests by removing shell-driven app reset from Espresso smoke test and enabling Android Test Orchestrator with `clearPackageData` for isolated app test execution.
