# Trello Agent Skill - End-to-End Examples

Date: 2026-02-15
Related:
- `docs/TRELLO_AGENT_SKILL.md`
- `docs/AI_IMPLEMENTATION_TASKS.md`

## Example 1: TASK-01 from card creation to DONE

### Input
- Task: `TASK-01: Clean up Gradle dependencies and duplicates`
- Start list: `TODO`

### Step A: Create card

```bash
curl --request POST \
  --url "https://api.trello.com/1/cards" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idList=<TODO_LIST_ID>" \
  --data-urlencode "name=[P1] Clean up Gradle dependencies" \
  --data-urlencode "desc=Goal:\nUnify dependencies and remove duplicates...\n\nScope:\napp/build.gradle\n\nAcceptance Criteria:\n- no duplicates\n- compile passes"
```

### Step B: Add checklist

```bash
curl --request POST \
  --url "https://api.trello.com/1/checklists" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idCard=<CARD_ID>" \
  --data-urlencode "name=TASK-01 Implementation"
```

Checklist items (add as separate requests):
- Inventory duplicate dependencies
- Remove duplicates and legacy dependencies
- Verify unused dependencies
- Run `:app:compileDebugKotlin`
- Update status in `docs/AI_IMPLEMENTATION_TASKS.md`

### Step C: Start work (move to In Progress)

```bash
curl --request PUT \
  --url "https://api.trello.com/1/cards/<CARD_ID>" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idList=<IN_PROGRESS_LIST_ID>"
```

Status comment:

```text
STATUS: IN_PROGRESS | TASK: TASK-01 | NEXT: remove duplicate material/lifecycle dependencies
```

### Step D: Iterative updates

After each code iteration:
1. mark relevant checklist item complete,
2. add `STATUS: IN_PROGRESS ...` comment.

### Step E: Close task

Final comment:

```text
STATUS: DONE | TASK: TASK-01 | RESULT: Removed duplicates and dead dependencies; compile passes. | VERIFY: ./gradlew :app:compileDebugKotlin
```

Move card to `Done`.

---

## Example 2: TASK-02 with TESTING stage

### Input
- Task: `TASK-02: Standardize JDK 17`
- Workflow: `TODO -> IN PROGRESS -> TESTING -> DONE`

### Step A: Move to In Progress + comment

```text
STATUS: IN_PROGRESS | TASK: TASK-02 | NEXT: add environment requirements and JDK verification instructions
```

### Step B: Implement
- add JDK 17 requirement to documentation,
- add commands: `java -version`, `./gradlew -version`,
- optionally update CI workflow.

### Step C: Move to TESTING

```text
STATUS: IN_PROGRESS | TASK: TASK-02 | NEXT: move to TESTING and validate JDK 17 setup
```

When implementation is complete, move card to `TESTING`.

### Step D: Accept and finish

Final comment:

```text
STATUS: DONE | TASK: TASK-02 | RESULT: JDK 17 setup and verification steps documented. | VERIFY: java -version && ./gradlew -version
```

Move `TESTING -> DONE`.

---

## Example 3: BLOCKED case and resume flow

### Situation
During `TASK-07`, migration away from `Serializable` is blocked by architectural decision (`Parcelable` vs ID-based contracts).

### Step A: Add BLOCKED comment

```text
STATUS: BLOCKED | TASK: TASK-07 | NEXT: decision required: Parcelable or ID-based contract
```

### Step B: Organize blocked work
- keep card in `IN PROGRESS` (board currently has no dedicated `BLOCKED` list),
- check only completed checklist items,
- add note with completed scope and pending decision.

### Step C: Resume
After decision is made:

```text
STATUS: IN_PROGRESS | TASK: TASK-07 | NEXT: implement selected contract strategy and run compile
```

Then proceed to `TESTING`/`DONE` as usual.

---

## Ready-to-Use Mini Prompts

### Prompt A: Create all cards from task document

```text
Use docs/TRELLO_AGENT_SKILL.md. Read docs/AI_IMPLEMENTATION_TASKS.md and create cards for TASK-01..TASK-09 in TODO. Add descriptions and checklists mapped 1:1 to implementation steps.
```

### Prompt B: Perform iterative status updates

```text
Use docs/TRELLO_AGENT_SKILL.md. For TASK-01, update Trello after every change: check checklist items, add STATUS comments, and move card through workflow lists.
```

### Prompt C: Close task after verification

```text
Use docs/TRELLO_AGENT_SKILL.md. Validate TASK-02 acceptance criteria, add final STATUS: DONE comment with VERIFY command, and move card to Done.
```
