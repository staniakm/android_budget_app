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
