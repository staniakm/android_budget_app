# InternetApi Android App

This is a Kotlin Android app that consumes data from the
[homeBudgetKotlinApi](https://github.com/staniakm/homeBudgetKotlinApi) backend.

## Tech Stack

- Kotlin
- Hilt
- Jetpack Compose (Material)
- Retrofit2

## Environment Requirements

- **JDK 17** (required and recommended for local development)
- Android SDK / Android Studio compatible with `compileSdkVersion 33`

## Build Verification

Use the commands below to verify local setup and build:

```bash
java -version
./gradlew -version
./gradlew :app:compileDebugKotlin
```

Expected outcome:
- `java -version` should report JDK 17.
- `:app:compileDebugKotlin` should complete successfully.

## Test Verification

Run local unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Compile Android instrumentation tests:

```bash
./gradlew :app:compileDebugAndroidTestKotlin
```

Run instrumentation tests on a connected emulator/device:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Note: `connectedDebugAndroidTest` requires at least one connected Android device or running emulator.

For reproducible local/CI mutation-regression checks, run targeted classes:

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.example.internetapi.ui.MutationFlowsInstrumentationTest
```

Recommended emulator profile for stable runs:
- Android API 35
- Medium Phone AVD
- Clean snapshot or cold boot before CI-like execution

If you hit a Unified Test Platform/protobuf crash in connected tests on AGP 7.x, use:

```properties
android.experimental.androidTest.useUnifiedTestPlatform=false
```

## Notes

- The app uses Activity-based navigation and Intent extras contracts.
- Project migration from XML screens to Compose is in progress and tracked in `docs/`.
