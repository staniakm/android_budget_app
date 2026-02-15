# Trello Agent Skill - Przyklady end-to-end

Data: 2026-02-15
Powiazanie:
- `docs/TRELLO_AGENT_SKILL.md`
- `docs/AI_IMPLEMENTATION_TASKS.md`

## Przyklad 1: TASK-01 od utworzenia karty do DONE

### Wejscie
- Zadanie: `TASK-01: Uporzadkowanie zaleznosci i duplikatow Gradle`
- Lista startowa: `Backlog`

### Krok A: Utworzenie karty

```bash
curl --request POST \
  --url "https://api.trello.com/1/cards" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idList=<ID_LISTY_BACKLOG>" \
  --data-urlencode "name=[P1] Uporzadkowac zaleznosci Gradle" \
  --data-urlencode "desc=Cel:\nUjednolicic zaleznosci i usunac duplikaty...\n\nZakres:\napp/build.gradle\n\nKryteria akceptacji:\n- brak duplikatow\n- compile przechodzi"
```

### Krok B: Dodanie checklisty

```bash
curl --request POST \
  --url "https://api.trello.com/1/checklists" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idCard=<ID_KARTY>" \
  --data-urlencode "name=Implementacja TASK-01"
```

Elementy checklisty (dodawane osobnymi requestami):
- Zinwentaryzowac duplikaty dependencies
- Usunac duplikaty i legacy dependencies
- Zweryfikowac nieuzywane dependencies
- Uruchomic `:app:compileDebugKotlin`
- Zaktualizowac status w `docs/AI_IMPLEMENTATION_TASKS.md`

### Krok C: Start pracy (przeniesienie do In Progress)

```bash
curl --request PUT \
  --url "https://api.trello.com/1/cards/<ID_KARTY>" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idList=<ID_LISTY_IN_PROGRESS>"
```

Komentarz statusowy:

```text
STATUS: IN_PROGRESS | TASK: TASK-01 | NEXT: usunac duplikaty material/lifecycle
```

### Krok D: Iteracyjne odhaczanie

Po kazdej zmianie:
1. odhacz odpowiedni check item,
2. dodaj komentarz `STATUS: IN_PROGRESS ...`.

### Krok E: Domkniecie

Komentarz koncowy:

```text
STATUS: DONE | TASK: TASK-01 | RESULT: Usunieto duplikaty i martwe zaleznosci, build przechodzi. | VERIFY: ./gradlew :app:compileDebugKotlin
```

Przeniesienie karty do `Done`.

---

## Przyklad 2: TASK-02 z przejsciem przez Review

### Wejscie
- Zadanie: `TASK-02: Standaryzacja JDK 17`
- Strategia: `Ready -> In Progress -> Review -> Done`

### Krok A: Przeniesienie do In Progress i komentarz

```text
STATUS: IN_PROGRESS | TASK: TASK-02 | NEXT: dodac sekcje wymagania srodowiskowe i instrukcje weryfikacji JDK
```

### Krok B: Implementacja
- dodanie dokumentacji JDK 17,
- opis komend: `java -version`, `./gradlew -version`,
- (opcjonalnie) aktualizacja workflow CI.

### Krok C: Przeniesienie do Review

```text
STATUS: IN_PROGRESS | TASK: TASK-02 | NEXT: review dokumentacji i potwierdzenie build na JDK 17
```

Po ukonczeniu implementacji: karta trafia do `Review`.

### Krok D: Akceptacja i Done

Komentarz koncowy:

```text
STATUS: DONE | TASK: TASK-02 | RESULT: Udokumentowano JDK 17 i procedure weryfikacji. | VERIFY: java -version && ./gradlew -version
```

Przeniesienie `Review -> Done`.

---

## Przyklad 3: Blokada (BLOCKED) i kontynuacja

### Sytuacja
Podczas `TASK-07` agent nie moze dokonczyc migracji `Serializable`, bo wymaga to decyzji architektonicznej (`Parcelable` vs przekazywanie ID).

### Krok A: Komentarz BLOCKED

```text
STATUS: BLOCKED | TASK: TASK-07 | NEXT: potrzebna decyzja: Parcelable czy kontrakt oparty o ID
```

### Krok B: Organizacja pracy
- karta zostaje w `In Progress` lub trafia do dedykowanej listy `Blocked` (jesli istnieje),
- odhacz tylko wykonane podpunkty checklisty,
- dodaj notatke co jest gotowe i co czeka na decyzje.

### Krok C: Wznowienie
Po decyzji architektonicznej:

```text
STATUS: IN_PROGRESS | TASK: TASK-07 | NEXT: wdrozyc wybrana strategie i uruchomic compile
```

Po wdrozeniu i weryfikacji przejscie do `Review`/`Done`.

---

## Gotowe mini-prompty

### Prompt A: Utworz wszystkie karty z dokumentu

```text
Uzyj docs/TRELLO_AGENT_SKILL.md. Wczytaj docs/AI_IMPLEMENTATION_TASKS.md i utworz karty dla TASK-01..TASK-09 na liscie Backlog. Dodaj opisy i checklisty 1:1 do krokow implementacyjnych.
```

### Prompt B: Prowadz iteracyjna aktualizacje statusu

```text
Uzyj docs/TRELLO_AGENT_SKILL.md. Dla TASK-01 aktualizuj Trello po kazdej zmianie: odhaczaj checklisty, dodawaj komentarz STATUS i przenos karte miedzy listami zgodnie z workflow.
```

### Prompt C: Domknij zadanie po weryfikacji

```text
Uzyj docs/TRELLO_AGENT_SKILL.md. Sprawdz kryteria akceptacji TASK-02, dodaj komentarz koncowy STATUS: DONE z komenda VERIFY i przenies karte do Done.
```
