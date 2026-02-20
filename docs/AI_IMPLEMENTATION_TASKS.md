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

**Status:** DONE

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

### Iterative progress
- [x] Iteration 1: located all current deprecated Serializable usages (`AccountUpdateActivity`, `UpdateBudgetActivity`).
- [x] Iteration 2 (tests-first): added instrumentation tests for Serializable compatibility helper (`BundleCompatTest`).
- [x] Iteration 3: added shared extension helper `Bundle.getSerializableCompat(...)`.
- [x] Iteration 4: migrated critical flows (`AccountUpdateActivity`, `UpdateBudgetActivity`) to helper-based safe reads.
- [x] Iteration 5: validated with `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, and `:app:compileDebugKotlin` on JDK 17.

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

**Status:** DONE

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

### Iterative progress
- [x] Iteration 1: scanned UI code and identified hardcoded messages and typos (`Faile`, `Invaliad`, mixed PL/EN strings).
- [x] Iteration 2 (tests-first): added androidTest coverage for string quality and typo regression checks (`UiStringsQualityTest`).
- [x] Iteration 3: added dedicated string resources for error/success/empty-state/dialog messages used in account, invoice, budget, and media flows.
- [x] Iteration 4: replaced selected hardcoded strings in `AccountUpdateActivity`, `UpdateBudgetActivity`, `AccountDetailsActivity`, `AccountIncomeDetails`, `AccountOutcomeDetails`, `AccountOutcomeRegisterActivity`, and `MediaActivity`.
- [x] Iteration 5: fixed typo-prone messages and normalized wording using shared resources.
- [x] Validation: `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, and `:app:compileDebugKotlin` pass on JDK 17.

### Acceptance criteria
- No new hardcoded strings in modified screens.
- Typos fixed and language usage consistent.

### Risks and notes
- If bilingual support is required, create separate follow-up tasks for `values-pl` / `values-en`.

---

## BUG-02: DatePicker visibility in dark mode (P2)

**Status:** DONE

### Goal
Ensure DatePicker content is readable when the app runs in dark mode.

### Scope
- `app/src/main/java/com/example/internetapi/functions/DatePicker.kt`
- Compose dialogs embedding platform `DatePicker` in:
  - `AccountDetailsActivity`
  - `AccountOutcomeRegisterActivity`
  - `MediaDetailsActivity`

### Implementation steps
1. Verify if target behavior is covered by tests.
2. Add tests first for the dark-mode theme resolution decision.
3. Introduce a shared DatePicker factory for dialog usage.
4. Apply factory in all affected screens.
5. Validate with unit tests and Kotlin compile.

### Iterative progress
- [x] Step 1: verified there was no dedicated test coverage for DatePicker dark-mode theming.
- [x] Step 2: added `DatePickerThemeTest` to cover dark/day theme resolution behavior.
- [x] Step 3: introduced `createDialogDatePicker(context)` and `resolveDatePickerThemeResId(uiMode)` helper in `functions/DatePicker.kt`.
- [x] Step 4: migrated DatePicker creation in `AccountDetailsActivity`, `AccountOutcomeRegisterActivity`, and `MediaDetailsActivity` to the shared helper.
- [x] Step 5: validated with `:app:testDebugUnitTest --tests com.example.internetapi.functions.DatePickerThemeTest` and `:app:compileDebugKotlin`.

### Acceptance criteria
- DatePicker remains readable in dark mode.
- A single shared DatePicker creation path is used in affected dialogs.
- Compile and tests pass after migration.

### Risks and notes
- This change forces a light DatePicker theme only when night mode is active to avoid low-contrast text.

---

## TASK-10: Increase regression test coverage for critical flows (P2)

**Status:** TESTING

### Goal
Increase automated regression coverage across unit, ViewModel mutation, and instrumentation layers.

### Scope
- Unit edge tests for invoice mapping/calculation behavior.
- ViewModel mutation tests for Budget and Media flows.
- Additional instrumentation launch coverage for budget mutation entry.
- Test execution notes in README for local/CI reproducibility.

### Implementation steps
1. Add unit tests for financial/mapping boundary cases.
2. Extend ViewModel mutation tests in uncovered critical modules.
3. Add meaningful instrumentation coverage for mutation journey entry points.
4. Validate compile and connected instrumentation test execution.
5. Document execution guidance in README/CI notes.

### Iterative progress
- [x] Step 1: added edge tests in `InvoiceModelTest` (blank shop and empty-item invoice sum).
- [x] Step 2: added `BudgetViewModelMutationTest` and `MediaViewModelMutationTest` for success/error mutation scenarios, then extended with extra mutation cases (`getBudgetItems`, `removeMediaUsage`, additional error paths).
- [x] Step 3: added `UpdateBudgetActivityLaunchTest` to cover update-budget mutation screen entry with required extras and missing-extra finish behavior.
- [x] Step 4: validated with `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, and targeted connected mutation instrumentation run.
- [x] Step 5: updated `README.md` with reproducible connected test command and emulator profile notes.
- [x] Additional expansion: extended AccountOutcome and Invoice ViewModel mutation tests (shop/item creation, invoice details, invoice creation error, delete flows) and added missing-extra instrumentation guard in `AccountUpdateActivityLaunchTest`.
- [x] Additional expansion: extended mutation instrumentation checks in `MutationFlowsInstrumentationTest` to assert critical add-income/transfer dialog inputs are present and stable, and validated `AccountUpdateActivity` missing-extra finish behavior.
- [x] Additional unit expansion: extended utility/mapping coverage in `AutocompleteFilterTest`, `MonthSwitcherTest`, `DatePickerThemeTest`, and `FormatterTest` with edge cases (trimmed queries, year boundaries, UI mode flags, negative/whole-number formatting).

### Acceptance criteria
- Expanded unit + ViewModel + instrumentation coverage for critical flows.
- Stable local/CI execution instructions documented.
- Regression risk reduced in create/update core mutation paths.

### Risks and notes
- Full connected suite may still include unrelated legacy failures; targeted mutation classes are documented for stable regression checks.

---

## TASK-16: Mutation UX consistency sweep (P3)

**Status:** TESTING

### Goal
Unify success/error/loading feedback across key mutation screens.

### Scope
- `app/src/main/java/com/example/internetapi/ui/AccountDetailsActivity.kt`
- `app/src/main/java/com/example/internetapi/ui/AccountOutcomeDetails.kt`
- `app/src/main/res/values/strings.xml`
- mutation message quality check in `UiStringsQualityTest`

### Implementation steps
1. Inventory mutation feedback patterns and hardcoded messages.
2. Standardize mutation success/error wording with string resources.
3. Align loading behavior in mutation flows.
4. Validate compile and mutation instrumentation smoke tests.

### Iterative progress
- [x] Step 1: identified hardcoded mutation-related messages and mixed loading behavior in AccountDetails/AccountOutcomeDetails.
- [x] Step 2: replaced hardcoded mutation feedback with resources (`success_invoice_removed`, invalid value feedback, dialog actions).
- [x] Step 3: aligned `AccountDetails` loading indicator to include mutation requests (`transfer` and `add income`) for consistent UX.
- [x] Step 4: validated with `:app:compileDebugKotlin`, `:app:compileDebugAndroidTestKotlin`, and targeted mutation instrumentation run `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.internetapi.ui.MutationFlowsInstrumentationTest`.

### Acceptance criteria
- Targeted mutation screens show consistent feedback pattern for success/error/loading.
- Mutation-related hardcoded messages are reduced and moved to resources.
- Compile checks and mutation smoke tests pass.

### Risks and notes
- Full `connectedDebugAndroidTest` may still include unrelated legacy failures; mutation-focused verification is covered by targeted instrumentation class.

---

## TASK-17: Add instrumentation tests for mutation flows (P2)

**Status:** TESTING

### Goal
Add UI-level regression protection for critical mutation flows.

### Scope
- Android instrumentation tests for key mutation scenarios:
  - add income flow,
  - transfer money flow,
  - invoice mutation screen launch (update/delete context).

### Implementation steps
1. Add test scenario for add income flow.
2. Add test scenario for transfer money flow.
3. Add test scenario for update invoice account and/or delete invoice flow.
4. Validate with `compileDebugAndroidTestKotlin` and `connectedDebugAndroidTest` on emulator/device.
5. Document execution notes for stable local/CI runs.

### Iterative progress
- [x] Step 1: added Compose instrumentation test that opens Add Income dialog from `AccountDetailsActivity`.
- [x] Step 2: added Compose instrumentation test that opens Transfer Money dialog from `AccountDetailsActivity`.
- [x] Step 3: added instrumentation launch test for `AccountOutcomeDetails` mutation screen context.
- [x] Step 4: validated androidTest source compilation with `:app:compileDebugAndroidTestKotlin`.
- [~] Step 4 (device run): `:app:connectedDebugAndroidTest` requires connected emulator/device in local environment.
- [x] Step 5: added task implementation notes in this document.

### Acceptance criteria
- At least 2-3 meaningful mutation instrumentation tests added.
- Tests compile and are ready to run on connected emulator/device.
- Critical mutation UI paths gain instrumentation regression coverage.

### Risks and notes
- Mutation tests relying on live API data can be flaky; current scenarios focus on stable dialog/open-entry interactions.

---

## TASK-18: Migrate ActivityResult Serializable extras to compat helper (P2)

**Status:** TESTING

### Goal
Remove remaining deprecated `getSerializableExtra` usage in ActivityResult result handling.

### Scope
- `app/src/main/java/com/example/internetapi/ui/AccountActivity.kt`
- `app/src/main/java/com/example/internetapi/ui/BudgetActivity.kt`
- `app/src/main/java/com/example/internetapi/functions/BundleCompat.kt`
- androidTest coverage for compat helper behavior

### Implementation steps
1. Verify existing coverage for Serializable compat result reads.
2. Add shared Intent compat helper for serializable extras.
3. Replace result handling reads in Account and Budget flows.
4. Validate compile and androidTest source compilation.

### Iterative progress
- [x] Step 1: verified existing `BundleCompatTest` coverage; identified missing direct Intent helper coverage.
- [x] Step 2: added `Intent.getSerializableExtraCompat(...)` in `BundleCompat.kt`.
- [x] Step 3: migrated ActivityResult handling in `AccountActivity` and `BudgetActivity` to `getSerializableExtraCompat`.
- [x] Step 4: added `IntentCompatTest` and validated with `:app:compileDebugKotlin` and `:app:compileDebugAndroidTestKotlin`.

### Acceptance criteria
- No deprecated `getSerializableExtra` usage remains in Account/Budget result flows.
- Account/Budget update result handling keeps previous behavior.
- Compile checks pass.

### Risks and notes
- This task focuses on ActivityResult result reads only and keeps existing Serializable contract unchanged.

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
