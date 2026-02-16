# AGENTS Playbook

## Project Snapshot

- Project: Android app (`InternetApi`) using Kotlin + Hilt + Compose (Material 1).
- Most former Activity XML screens were migrated to Compose.
- Build is currently validated with JDK 11 in this environment (`:app:compileDebugKotlin` passes).

## Recent Migration Status

- Migrated to Compose in this workstream:
  - `AccountDetailsActivity`
  - `AccountOutcomeRegisterActivity`
  - `AccountOutcomeDetails`
  - `ChartActivity` (chart sizing restored to near full-screen behavior)
- Legacy unused layout resources were removed from `app/src/main/res/layout`.
- Removed dead navigation template resource: `app/src/main/res/navigation/nav_graph.xml`.

## Task Tracking Docs

- Main implementation task plan: `docs/AI_IMPLEMENTATION_TASKS.md`
- Tech debt review: `docs/TECH_DEBT_REVIEW.md`
- Trello skill guide: `docs/TRELLO_AGENT_SKILL.md`
- Trello end-to-end examples: `docs/TRELLO_AGENT_SKILL_EXAMPLES.md`

## Explicit User Rules (Must Follow)

1. Do not use any Git commands for production code work.
2. New files may be added to Git only in test-related work.
3. Every new markdown/documentation file must be written in English.

## Operating Notes For Next Sessions

- Make changes iteratively and update progress in documentation after each significant step.
- Prefer safe, minimal-scope edits and re-run compile checks after each iteration.
- Keep existing architecture patterns unless the task explicitly asks for refactor.
- Preserve intent extras contracts and existing ViewModel/API behavior during UI changes.

## Implementation Guidelines (Future Work)

- Build and verification:
  - Prefer JDK 17 for local validation (`C:\Users\Mariusz\.jdks\corretto-17.0.11` in this environment).
  - Standard verification command for code changes: `./gradlew :app:compileDebugKotlin`.
  - For test tasks, additionally run `./gradlew :app:testDebugUnitTest` and compile android tests with `./gradlew :app:compileDebugAndroidTestKotlin`.
  - Before starting any production change, verify whether the target behavior is covered by tests.
  - If coverage is missing for the changed behavior, add or update tests first (test-first approach) before implementing production code changes.

- Task and Trello process:
  - Every new implementation must be based on an existing Trello task/card.
  - If a task does not exist on Trello, define/create it before starting implementation.
  - Move the Trello card to `IN PROGRESS` when implementation starts.
  - Move the Trello card to `DONE` after implementation is completed and validated.

- Trello workflow alignment:
  - Board flow is `TODO -> IN PROGRESS -> TESTING -> DONE`.
  - Keep Trello card status synchronized with implementation progress.
  - Add status comments in format: `STATUS: <...> | TASK: <...> | NEXT/RESULT: <...>`.

- Compose state pattern:
  - Use shared `observeResource(liveData)` helper for `LiveData<Resource<T>>?` observation in composables.
  - Avoid repeating per-screen `DisposableEffect + Observer` boilerplate when helper behavior is sufficient.
  - Keep side effects in `LaunchedEffect` and request creation in `remember` blocks.

- Documentation and language:
  - Keep all new documentation in English.
  - When conventions change (workflow, build path, testing), update `AGENTS.md` and relevant docs in the same task.
