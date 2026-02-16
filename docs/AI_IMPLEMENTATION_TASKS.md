# Proposed Changes - AI-Ready Implementation Tasks

Date: 2026-02-15
Related: `docs/TECH_DEBT_REVIEW.md`

## How to Use This Document

Each task includes:
- **Goal** (what and why),
- **Scope** (what can be changed),
- **Implementation steps** (recommended order),
- **Acceptance criteria** (definition of done),
- **Risks and notes** (important constraints).

This format is AI-ready so an agent can execute tasks without extra business clarification.

---

## TASK-01: Clean up Gradle dependencies and duplicates (P1)

**Status:** DONE

### Goal
Unify dependencies and remove duplicates to reduce conflicts and simplify upgrades.

### Scope
- `app/build.gradle`
- optionally `build.gradle` (if needed for version consistency)

### Implementation steps
1. Inventory duplicated and inconsistent dependencies.
2. Remove duplicates (for example, repeated `material`, `lifecycle-viewmodel-ktx`).
3. Align AndroidX/Material/Lifecycle/Coroutines versions to compatible sets.
4. Keep compatibility with current AGP/Kotlin (no major toolchain migration in this task).
5. Run compile validation: `./gradlew :app:compileDebugKotlin`.

### Iterative progress
- [x] Step 1: duplicated and potentially unused dependencies identified.
- [x] Step 2 (iteration 1): removed duplicate `material` and `lifecycle-viewmodel-ktx`; removed legacy `android.arch.lifecycle:extensions`.
- [x] Step 2 (iteration 2): removed unused `navigation-*` and `fragment-ktx`; removed dead `res/navigation/nav_graph.xml` template resource.
- [x] Step 3: completed safe version alignment (Coroutines/Activity/Lifecycle) without AGP/Kotlin migration.
- [x] Step 4: compatibility with current AGP/Kotlin preserved.
- [x] Step 5: `:app:compileDebugKotlin` passes after each iteration.

### Acceptance criteria
- No duplicate dependencies in `app/build.gradle`.
- `:app:compileDebugKotlin` succeeds.
- No functional regression introduced by version/dependency changes.

### Risks and notes
- Avoid AGP/Kotlin major upgrades in this task.
- If one dependency upgrade requires another, document it in task notes/PR comment.

---

## TASK-02: Standardize JDK 17 for the project (P1)

**Status:** DONE

### Goal
Remove build instability caused by running Gradle on unsupported JDK versions.

### Scope
- project documentation (README or `docs/`)
- build configuration only when necessary and safe

### Implementation steps
1. Add an "Environment requirements" section with explicit JDK 17 guidance.
2. Add quick verification commands (`java -version`, `./gradlew -version`).
3. If CI exists, pin Java 17 and validate workflow syntax.
4. Verify local compile on JDK 17: `./gradlew :app:compileDebugKotlin`.

### Iterative progress
- [x] Step 1 (iteration 1): added JDK 17 requirements in `README.md`.
- [x] Step 2 (iteration 1): added setup verification commands in `README.md`.
- [x] Step 3: repository has no CI workflow files (`.github/workflows` is empty), so no CI update was required.
- [x] Step 4: local compile validated on JDK 17 (`C:\Users\Mariusz\.jdks\corretto-17.0.11`, `:app:compileDebugKotlin`).
- [x] Additional validation: compile still passes on JDK 11 (`JAVA_HOME=/c/Program Files/Java/jdk-11`, `:app:compileDebugKotlin`).

### Acceptance criteria
- Documentation clearly states JDK 17 support requirement.
- Build is reproducible using documented setup.

### Risks and notes
- Do not remove `kapt.jvmargs` workarounds if that would destabilize builds.

---

## TASK-03: Move namespace from manifest to Gradle (P3)

**Status:** DONE

### Goal
Remove AGP warning about `package` in AndroidManifest and align with modern configuration.

### Scope
- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`

### Implementation steps
1. Add `namespace "com.example.internetapi"` in app module.
2. Verify `applicationId` remains unchanged.
3. Remove/adjust manifest `package` according to AGP requirements.
4. Run: `./gradlew :app:processDebugMainManifest :app:compileDebugKotlin`.

### Iterative progress
- [x] Step 1 (iteration 1): added `namespace "com.example.internetapi"` in `app/build.gradle`.
- [x] Step 2 (iteration 1): `applicationId` remains `com.example.internetapi`.
- [x] Step 3 (iteration 1): removed `package` from `app/src/main/AndroidManifest.xml`.
- [x] Step 4: validated with `:app:processDebugMainManifest :app:compileDebugKotlin` on JDK 17.

### Acceptance criteria
- Namespace warning is removed or significantly reduced.
- Build passes.

### Risks and notes
- Do not rename Kotlin/Java package declarations.

---

## TASK-04: Add a minimal regression test baseline (P1)

**Status:** DONE

### Goal
Create a basic safety net against regressions after Compose migrations.

### Scope
- `app/src/test/...`
- `app/src/androidTest/...` (minimal but meaningful)

### Implementation steps
1. Add unit tests for formatters and request mapping helpers.
2. Add ViewModel tests with mocked repositories (at least 3 critical success/error cases).
3. Add 1-2 critical UI/instrumented tests (for example invoice save flow).
4. Add test execution instructions to docs.

### Iterative progress
- [x] Iteration 1: added test dependencies for unit and instrumented tests (`core-testing`, `kotlinx-coroutines-test`, `androidx.test:core-ktx`).
- [x] Iteration 2: added unit tests for formatters and invoice/request mapping (`FormatterTest`, `InvoiceModelTest`).
- [x] Iteration 3: added ViewModel tests with success/error paths (`AccountOutcomeViewModelTest`).
- [x] Iteration 4: added instrumented UI test baseline (`MainActivityLaunchTest`) and verified androidTest source compilation.
- [x] Iteration 5: added test execution instructions to `README.md`.
- [x] Validation: `:app:testDebugUnitTest` passes.
- [x] Validation: `:app:compileDebugAndroidTestKotlin` passes.
- [~] Validation: `:app:connectedDebugAndroidTest` executed but no tests were run in current environment (requires connected emulator/device).

### Acceptance criteria
- Tests run locally.
- Coverage includes at least happy and error paths for critical flows.

### Risks and notes
- Do not target high coverage percentage yet; this is an MVP safety net.

---

## TASK-05: Standardize Compose UI state pattern (P2)

**Status:** DONE

### Goal
Reduce boilerplate and inconsistent side-effect handling across Compose screens using LiveData.

### Scope
- selected screens in `app/src/main/java/com/example/internetapi/ui`
- optional helper module/file for shared UI state conventions

### Implementation steps
1. Define one consistent pattern (`UiState + UiEvent` or equivalent).
2. Apply it on one pilot screen (for example `AccountOutcomeDetails`).
3. Ensure loading/error/success handling is uniform.
4. Extend to 2-3 additional similar screens.
5. Add a short convention guide in `docs/`.

### Iterative progress
- [x] Iteration 1: introduced shared Compose helper `observeResource(...)` in `MainActivity.kt` to unify LiveData-to-Resource observation.
- [x] Iteration 2: applied helper on `AccountIncomeDetails` (removed ad-hoc observer boilerplate).
- [x] Iteration 3: applied helper on `UpdateBudgetActivity` (for both list and update requests).
- [x] Iteration 4: applied helper on `AccountDetailsActivity` (operations and income type requests).
- [x] Iteration 5: compile validated and pattern usage summary documented below.

### Compose state convention (short guide)
- Use `observeResource(liveData)` for UI observation of `LiveData<Resource<T>>?` in composables.
- Keep request triggering in `remember(...)` blocks and side effects in `LaunchedEffect(...)`.
- Keep UI messaging (`snackbar`) driven by `resource.status` checks.
- For optional requests, pass `null` LiveData and let `observeResource` return `null` safely.
- Avoid repeating per-screen `DisposableEffect + Observer` boilerplate when no custom behavior is needed.

### Acceptance criteria
- At least 3 screens no longer use ad-hoc side-effect patterns.
- Code is clearer and/or shorter without losing functionality.

### Risks and notes
- Avoid a big-bang refactor across all screens at once.

---

## TASK-06: Standardize mutating operation handling (P2)

**Status:** DONE

### Goal
Provide predictable user feedback for add/update/delete operations.

### Scope
- ViewModel + UI for mutating flows (accounts, invoices, media, budgets)

### Implementation steps
1. Inventory fire-and-forget methods (for example methods returning `Unit`).
2. Update contracts to expose observable result (`Resource`/status).
3. Add consistent loading/success/error handling in UI.
4. Normalize snackbar messages and trigger points.

### Iterative progress
- [x] Iteration 1: identified a coverage gap for mutating account flows (`addIncome`, `transferMoney`) and confirmed missing dedicated tests.
- [x] Iteration 2: added tests first for mutation result handling (`AccountViewModelMutationTest`).
- [x] Iteration 3: refactored `AccountViewModel.addIncome` to return observable `LiveData<Resource<List<AccountIncome>>>`.
- [x] Iteration 4: updated `AccountDetailsActivity` mutation flow to observe transfer/add-income results and show consistent success/error feedback.
- [x] Iteration 5: validated with `:app:testDebugUnitTest` and `:app:compileDebugKotlin` on JDK 17.
- [x] Iteration 6: added tests first for invoice account-change mutation (`InvoiceViewModelMutationTest`).
- [x] Iteration 7: updated `AccountOutcomeDetails` to observe `updateInvoiceAccount` result and provide consistent success/error feedback.
- [x] Iteration 8: revalidated with `:app:testDebugUnitTest` and `:app:compileDebugKotlin` on JDK 17.
- [x] Scope closure: verified target mutation flows (accounts, invoices, media, budgets) expose observable results with consistent UI feedback in current implementation scope.

### Acceptance criteria
- Every mutating operation has visible user feedback.
- No silent failures.

### Risks and notes
- Contract changes may affect multiple screens; implement incrementally.

---

## TASK-07: Migrate Intent contracts away from Serializable (P2)

### Goal
Remove deprecated API usage and reduce runtime cast risks.

### Scope
- Activity intent contracts (`Intent extras`) in `ui/*`
- models passed between screens

### Implementation steps
1. Locate all `getSerializable()` and `getSerializableExtra()` usages.
2. Choose strategy: `Parcelable` or ID-based fetching.
3. Migrate critical flows first:
   - account update,
   - budget update,
   - other warning-producing paths.
4. Add helper/extension functions for safe extras reading.
5. Validate build and manually test navigation.

### Acceptance criteria
- No deprecated `getSerializable*` in critical flows.
- Navigation works without cast warnings.

### Risks and notes
- Parcelable migration may require model changes and compatibility checks.

---

## TASK-08: Deprecated API and warning cleanup (P3)

**Status:** DONE

### Goal
Reduce compiler warnings and prepare for future upgrades.

### Scope
- files reported by compiler warnings

### Implementation steps
1. Replace `toUpperCase(Locale.ROOT)` with `uppercase(Locale.ROOT)`.
2. Update deprecated UI APIs (`adapterPosition`, `setColorFilter`, others from warnings).
3. Re-run compile and close low-risk warnings.

### Iterative progress
- [x] Iteration 1: verified test coverage for changed behavior scope (invoice uppercase logic already covered by `InvoiceModelTest`).
- [x] Iteration 2: replaced deprecated `toUpperCase(Locale.ROOT)` with `uppercase(Locale.ROOT)` in `Invoice.kt`.
- [x] Iteration 3: replaced deprecated adapter position access (`adapterPosition` -> `bindingAdapterPosition`) in category/media adapters.
- [x] Iteration 4: replaced deprecated progress drawable coloring (`setColorFilter`) with `setTint` in `MonthBudgetAdapter`.
- [x] Iteration 5: removed an additional compile warning in `UpdateBudgetActivity` (unnecessary safe call in success branch).
- [x] Validation: `:app:testDebugUnitTest` and `:app:compileDebugKotlin` pass on JDK 17.
- [~] Remaining deprecated path: `getSerializable(...)` is intentionally left for `TASK-07` (Intent contract migration).

### Acceptance criteria
- Meaningful warning reduction.
- No functional regressions.

### Risks and notes
- Warnings can be fixed incrementally across multiple PRs.

---

## TASK-09: Message consistency and typo cleanup (P3)

### Goal
Improve UX quality and prepare for internationalization.

### Scope
- `strings.xml`
- hardcoded strings in Compose screens

### Implementation steps
1. Find hardcoded strings in `ui/*.kt`.
2. Move them to `strings.xml`.
3. Fix typos and mixed language inconsistencies.
4. Verify new string resources are used instead of literals.

### Acceptance criteria
- No new hardcoded strings in modified screens.
- Typos fixed and language usage consistent.

### Risks and notes
- If bilingual support is required, create separate follow-up tasks for `values-pl` / `values-en`.

---

## Suggested Execution Order

1. TASK-01
2. TASK-02
3. TASK-04
4. TASK-06
5. TASK-07
6. TASK-08
7. TASK-09
8. TASK-03
9. TASK-05

Note: TASK-03 and TASK-05 can be moved depending on stabilization/testing priorities.
