# SSC Kotlin POC (Multi-Club Ski Club Platform)

Kotlin-first Android reference app for a configurable, invite-only ski club platform.  
Goal: preserve critical booking/payment/governance rules while removing Sterling-specific hardcoding.

## What This Is

This project demonstrates an Android architecture for a real operational domain:
- Invite-only auth and distribution
- Membership dues gate before booking
- Configurable membership year (example: Nov 1 -> Oct 31)
- Bed-level booking flow (hold -> payment -> confirm)
- Stripe dues + booking payment orchestration
- RBAC committee controls
- Data-driven lodges/documents/assets

Core hard constraints:
- Coroutines + Flow only (no RxJava)
- Retrofit + OkHttp
- Jetpack Compose + Navigation
- Room + Hilt + WorkManager
- Espresso + UI Automator coverage

## Current Phase

Phase 8 active: release flavoring + CI hardening + runbook finalization.

## Architecture

```
UI (Compose + Navigation)
  -> Feature modules (invite-auth, membership, booking, docs, committee, profile)
    -> Domain/use-case layer (core model + policy)
      -> Data layer (Room + Retrofit repos)
        -> Network/Storage (OkHttp/Retrofit, Room)
          -> Background orchestration (WorkManager)
```

Principles:
- Multi-club by `club_id` everywhere
- No club-specific literals in business logic
- Server-driven config for lodges/docs/assets
- API-enforced RBAC + UI capability gating

## Tech Stack

| Layer | Tech |
|---|---|
| Language | Kotlin |
| Build | Gradle (wrapper) |
| UI | Jetpack Compose, Navigation |
| Async | Kotlin Coroutines, Flow |
| Network | Retrofit, OkHttp, Moshi |
| Local Data | Room |
| DI | Hilt |
| Background | WorkManager |
| Tests | JUnit4, Espresso, UI Automator |
| CI | GitHub Actions |

## Build Flavors

- `demo` flavor:
  - API base URL fixed to `https://example.com/`
  - deterministic bootstrap ids: `demo-club`, `demo-member`
  - app label: `Club POC Demo`
- `prod` flavor:
  - API base URL from `-PprodApiBaseUrl=...` or `PROD_API_BASE_URL`
  - fallback if unset: `https://example.com/`
  - bootstrap ids: `prod-club`, `prod-member`
  - app label: `Club POC`

## Project Structure

```
ssc-kotlin-poc/
├── app/
├── core/
│   ├── auth/
│   ├── common/
│   ├── database/
│   ├── model/
│   ├── network/
│   ├── payments/
│   ├── testing/
│   └── work/
├── feature/
│   ├── invite-auth/
│   ├── membership/
│   ├── lodge-catalog/
│   ├── booking/
│   ├── documents/
│   ├── committee-admin/
│   └── profile/
├── .github/workflows/android.yml
```

## Current Delivery

- Phase 4: RBAC + config-driven content complete:
  - `club_config` feature flags (`docs_enabled`, `lodges_enabled`, `assets_enabled`)
  - `member_roles`, `lodges`, `documents`
  - policy at `/Users/steve/git/personal/ssc-kotlin-poc/core/auth/src/main/java/com/club/poc/core/auth/CommitteeAccessPolicy.kt`
- Phase 5: UI redesign + instrumentation hardening complete:
  - custom theme + updated screen layouts across Invite/Membership/Home/Documents/Committee/Profile
  - Espresso smoke retained, navigation/system flow moved to UI Automator for Compose-safe coverage
- Phase 6: booking flow demo upgraded from placeholder to interactive:
  - bed-night inventory seeded in bootstrap
  - one-bed-per-person-per-night selection enforcement in UI + DAO constraints
  - hold creation via `BookingLifecycleDao`
  - Stripe booking payment simulated intent in-app, then hold confirm to booking record
  - booking state/viewmodel wiring:
    - `/Users/steve/git/personal/ssc-kotlin-poc/app/src/main/java/com/club/poc/app/BookingViewModel.kt`
    - `/Users/steve/git/personal/ssc-kotlin-poc/app/src/main/java/com/club/poc/app/data/BookingExperienceRepository.kt`
- Phase 7: booking checkout and hold ops hardening:
  - Stripe orchestration path integrated (`StripeCheckoutOrchestrator`)
  - automatic demo fallback when API base is placeholder or network checkout fails
  - explicit hold cancellation path (`ACTIVE -> CANCELLED`) with bed-night release
  - new DAO instrumentation coverage for cancel-hold lifecycle

## Prerequisites (macOS, Apple Silicon)

- Java 17 (Temurin/OpenJDK)
- Android Studio
- Android SDK packages:
  - `cmdline-tools;latest`
  - `platform-tools`
  - `emulator`
  - `build-tools;34.0.0`
  - `platforms;android-34`
  - `system-images;android-34;google_apis;arm64-v8a`
- AVD: `Pixel8_API_34` (or equivalent API 34 arm64 image)

## Environment Variables

Set in shell profile (`~/.zshrc`):

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export ANDROID_SDK_ROOT=$HOME/Library/Android/sdk
export PATH="/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/emulator:$PATH"
```

## Getting Started

```bash
cd /Users/steve/git/personal/ssc-kotlin-poc

# Verify toolchain
java -version
sdkmanager --version
adb version
emulator -version

# Build metadata / tasks
./gradlew tasks

# Build demo app
./gradlew :app:assembleDemoDebug
```

API note:
- Retrofit base URL must end with `/`.
- Demo uses placeholder (`https://example.com/`) and auto-fallback checkout.
- Prod override examples:
```bash
./gradlew :app:assembleProdDebug -PprodApiBaseUrl=https://api.your-club.example/
PROD_API_BASE_URL=https://api.your-club.example/ ./gradlew :app:assembleProdDebug
```

## Run App Locally

```bash
# Start emulator (if not running)
emulator -avd Pixel8_API_34

# Install/run from Android Studio (recommended), or:
./gradlew :app:installDemoDebug

# Optional explicit launch
adb shell monkey -p com.club.poc -c android.intent.category.LAUNCHER 1
```

Manual smoke path:
1. Launch app.
2. Invite screen -> `Accept Invite and Continue`.
3. Membership screen -> `Cycle Status (test)` then `Continue to Club Home`.
4. Home screen:
   - verify booking button disables for `UNPAID`/`LAPSED`
   - verify docs button follows docs feature toggle
5. Profile screen:
   - toggle `Reservationist`, `Docs Committee`
   - toggle `Documents` / `Lodges` feature flags
6. Committee/Documents screens:
   - verify capability state changes immediately.
7. Booking screen:
   - select up to one bed per night
   - tap `Create Hold`
   - tap `Process Stripe Payment + Confirm`
   - verify booking confirmation id + Stripe intent shown
   - optional: tap `Cancel Active Hold` before confirm to verify inventory release.

Work scheduling check:
```bash
adb shell dumpsys jobscheduler com.club.poc | grep "SystemJobService"
```

## Testing

Unit tests:
```bash
./gradlew testDebugUnitTest testDemoDebugUnitTest
```

Instrumentation tests (device/emulator required):
```bash
./gradlew :core:database:connectedDebugAndroidTest :app:connectedDemoDebugAndroidTest
```

Phase 8 local verify sequence:
```bash
./gradlew :app:assembleDemoDebug
./gradlew lintDemoDebug testDebugUnitTest testDemoDebugUnitTest
./gradlew :core:database:connectedDebugAndroidTest :app:connectedDemoDebugAndroidTest
```

Current baseline tests:
- Unit:
  - `core/model/src/test/.../MembershipYearPolicyTest.kt`
  - `core/auth/src/test/.../InviteOnlyAccessPolicyTest.kt`
  - `core/auth/src/test/.../CommitteeAccessPolicyTest.kt`
  - `core/auth/src/test/.../NetworkInviteAuthRepositoryTest.kt`
  - `core/payments/src/test/.../MembershipDuesGateTest.kt`
  - `core/payments/src/test/.../NetworkStripePaymentRepositoryTest.kt`
- Instrumented:
  - `core/database/src/androidTest/.../BookingLifecycleDaoTest.kt`
  - `app/src/androidTest/.../EspressoSmokeTest.kt`
  - `app/src/androidTest/.../UiAutomatorSmokeTest.kt`

Current count:
- Unit tests: 15
- Instrumentation tests: 10

## CI

Workflow: `/Users/steve/git/personal/ssc-kotlin-poc/.github/workflows/android.yml`

Jobs:
- `unit-and-static`: `assembleDemoDebug` + `lintDemoDebug` + `testDebugUnitTest` + `testDemoDebugUnitTest`
- `android-instrumented`: Ubuntu runner + API 34 `x86_64` emulator + `connectedDebugAndroidTest` (`core:database`) and `connectedDemoDebugAndroidTest` (`app`)
- both jobs upload test/lint artifacts


## Product Rules Preserved

- Invite-only access/distribution model
- Dues-paid prerequisite before booking
- Membership-year policy configurable by club
- One bed per person per night invariant
- Hold-based booking with payment confirmation
- Committee role-based modification rights
- Documents/lodges/assets driven by config/data
