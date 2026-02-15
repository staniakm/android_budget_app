# Technical Debt Review

Date: 2026-02-15

## Scope

This review covers:
- build and dependency layer (`build.gradle`, `app/build.gradle`, `gradle.properties`),
- UI layer after Compose migrations (`app/src/main/java/com/example/internetapi/ui`),
- quality signals (compiler warnings, TODOs, tests).

## TL;DR

The largest debt is in 4 areas:
1. **Outdated and fragile toolchain/dependencies** (JDK/AGP/Kotlin/Compose plus kapt workaround).
2. **Low testability and lack of regression tests** (effectively no real coverage).
3. **Inconsistent Compose + LiveData state/side-effect patterns** (boilerplate and error-prone code).
4. **Navigation contracts based on string extras + deprecated Serializable APIs**.

## Detailed Findings

| Priority | Area | Problem | Evidence | Impact | Effort | Complexity |
|---|---|---|---|---|---|---|
| P1 | Build/toolchain | JDK 21 incompatibility with old AGP/Kotlin stack; forced `kapt` exports | `gradle.properties:10-14`, `build.gradle:3-6`, `app/build.gradle:10,47-53` | Build instability and growing maintenance cost on environment updates | 2-4 days | Medium |
| P1 | Dependencies | Duplicate/inconsistent library versions (e.g., duplicate Material, old lifecycle/coroutines) | `app/build.gradle:58,72,75,82,78-79,92` | Harder debugging, possible conflicts, slower upgrades | 1-2 days | Low/Medium |
| P1 | Testing | Nearly no tests (template-only) | `app/src/test/.../ExampleUnitTest.kt`, `app/src/androidTest/.../ExampleInstrumentedTest.kt` | High regression risk during further migrations | 3-6 days (MVP) | Medium |
| P2 | Compose state | Repeated `LiveData.observe + DisposableEffect` boilerplate and manual refresh keys | multiple screens in `ui/*.kt` (e.g. `AccountOutcomeRegisterActivity.kt`, `AccountDetailsActivity.kt`) | Higher change cost, inconsistent behavior risk | 3-5 days | Medium/High |
| P2 | Side effects | Mutating operations without consistent result observation (fire-and-forget in places) | e.g., `AccountViewModel.addIncome()` returns `Unit` (`AccountViewModel.kt:41-44`) | Risk of silent failures and inconsistent UX | 2-4 days | Medium |
| P2 | Navigation contracts | Intent extras by string keys + deprecated `Serializable` usage | `AccountActivity.kt:129`, `BudgetActivity.kt:137`, `AccountUpdateActivity.kt:55`, `UpdateBudgetActivity.kt:63` | Fragile screen contracts, runtime cast error risk | 2-3 days | Medium |
| P3 | Deprecated APIs | Multiple deprecated API usages | warnings: `Invoice.kt:16`, `ChartActivity.kt:72-73`, adapters (`adapterPosition`, `setColorFilter`) | Not blocking now, but increases upgrade cost | 1-2 days | Low |
| P3 | Code quality | Typos and mixed language messages (PL/EN) | e.g., "Faile to revmoce", "Invaliad" in UI messages | Worse UX and harder internationalization | 0.5-1 day | Low |
| P3 | Android config | Namespace still sourced from `AndroidManifest package` (AGP warning) | build warning + `AndroidManifest.xml:3` | Small current impact, but required for AGP modernization | 0.5 day | Low |

## Proposed Debt Reduction Plan

### Phase 1 (highest ROI, low/moderate effort)
1. Clean up dependencies and versions in `app/build.gradle` (remove duplicates; align Material/Lifecycle/Coroutines).
2. Standardize supported JDK version (17) and update docs + CI/IDE config.
3. Add `namespace` in Gradle and remove reliance on manifest `package`.

**Estimated effort:** 2-4 days, **complexity:** medium.

### Phase 2 (functional stability)
1. Add a minimal test package:
   - 5-10 unit tests for formatters/mappers/request builders,
   - 3-5 ViewModel integration tests (mock repository),
   - 1-2 critical UI tests (happy paths) for migrated screens.

**Estimated effort:** 3-6 days, **complexity:** medium.

### Phase 3 (UI architecture)
1. Standardize state pattern (e.g., `UiState + UiEvent`) and reduce manual `observe` usage in composables.
2. Return explicit results for mutating operations and handle statuses consistently.
3. Gradually replace `Serializable` with safer contracts (Parcelable/typed arguments).

**Estimated effort:** 5-10 days (iterative), **complexity:** medium/high.

## Recommended Order

1. **Build/dependency cleanup** (quick win, unblocks future work).
2. **Regression test baseline** for critical flows.
3. **Compose state/side-effect refactor**.
4. **Intent contract migration** away from `Serializable`.

## Overall Assessment

- **Technical debt level:** medium-high.
- **Short-term risk:** medium (project builds, but is fragile during changes).
- **Mid-term risk:** high without tests and toolchain cleanup.
- **Business priority:** high for stabilization before adding more features.
