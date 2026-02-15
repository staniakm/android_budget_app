# Trello Agent Skill

## Cel

Ten skill definiuje standard pracy agenta AI z tablica Trello dla projektu `InternetApi`.

## Zakres odpowiedzialnosci agenta

Agent moze:
- tworzyc karty z zadan technicznych,
- aktualizowac opisy i checklisty,
- przenosic karty miedzy listami workflow,
- dodawac komentarze statusowe,
- oznaczac zadania jako done po spelnieniu kryteriow akceptacji.

Agent nie powinien:
- usuwac kart bez wyraznego polecenia,
- zamykac/archiwizowac tablicy,
- zmieniac uprawnien czlonkow tablicy.

## Wymagane dane konfiguracyjne

Przed praca agent musi miec dostep do:
- `TRELLO_API_KEY`
- `TRELLO_TOKEN`
- `TRELLO_BOARD_ID`

Opcjonalnie:
- `TRELLO_DEFAULT_LABEL_TECH_DEBT`
- `TRELLO_DEFAULT_LABEL_BUG`
- `TRELLO_DEFAULT_LABEL_FEATURE`

## Workflow tablicy

Rekomendowane listy:
1. `Backlog`
2. `Ready`
3. `In Progress`
4. `Review`
5. `Done`

Zasady przejsc:
- nowa karta trafia do `Backlog` albo `Ready` (wg polecenia),
- rozpoczecie prac: `Ready -> In Progress`,
- po implementacji: `In Progress -> Review`,
- po akceptacji: `Review -> Done`.

## Format karty zadania

### Tytul

`[PRIORYTET] Krotki czasownik + obiekt`

Przyklad:
- `[P1] Uporzadkowac zaleznosci Gradle`

### Opis

Kazdy opis powinien zawierac sekcje:
1. `Cel`
2. `Zakres`
3. `Kroki implementacyjne`
4. `Kryteria akceptacji`
5. `Ryzyka`

### Checklist

Checklista musi mapowac sie 1:1 do krokow implementacyjnych.

Przyklad:
- [ ] Zinwentaryzowac duplikaty dependencies
- [ ] Usunac duplikaty
- [ ] Uruchomic compile
- [ ] Zaktualizowac dokumentacje statusu

## Standard komentarzy statusowych

Format komentarza:

`STATUS: <IN_PROGRESS|BLOCKED|DONE> | TASK: <id/nazwa> | NEXT: <kolejny krok>`

Przyklad:

`STATUS: IN_PROGRESS | TASK: TASK-01 | NEXT: usunac nieuzywane navigation dependencies`

## Reguly aktualizacji statusu

Agent po kazdej istotnej zmianie powinien:
1. zaktualizowac checkliste na karcie,
2. dodac komentarz statusowy,
3. jezeli spelnione kryteria akceptacji - przeniesc karte do `Review` lub `Done`.

## Definicja DONE

Karta jest `DONE`, gdy:
- wszystkie punkty checklisty sa odhaczone,
- kryteria akceptacji z opisu sa spelnione,
- build/testy wymagane przez zadanie przeszly,
- status jest odnotowany komentarzem koncowym.

## Szablon komentarza koncowego

`STATUS: DONE | TASK: <id/nazwa> | RESULT: <1-2 zdania o wyniku> | VERIFY: <komenda lub sposob weryfikacji>`

## Minimalny zestaw operacji API (referencyjnie)

Agent powinien wspierac operacje:
- utworzenie karty,
- pobranie kart z listy,
- aktualizacje opisu,
- dodanie checklisty i elementow,
- odhaczenie elementu checklisty,
- przeniesienie karty miedzy listami,
- dodanie komentarza.

## Przykladowe operacje API Trello (REST)

Poniższe przyklady zakladaja, ze agent korzysta z Trello REST API v1.

### 1) Pobranie list na tablicy

```bash
curl "https://api.trello.com/1/boards/${TRELLO_BOARD_ID}/lists?key=${TRELLO_API_KEY}&token=${TRELLO_TOKEN}"
```

Cel: odczyt `id` list `Backlog`, `Ready`, `In Progress`, `Review`, `Done`.

### 2) Utworzenie karty

```bash
curl --request POST \
  --url "https://api.trello.com/1/cards" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idList=<ID_LISTY_BACKLOG>" \
  --data-urlencode "name=[P1] Uporzadkowac zaleznosci Gradle" \
  --data-urlencode "desc=Cel:\n...\n\nZakres:\n...\n\nKryteria akceptacji:\n..."
```

### 3) Dodanie checklisty do karty

```bash
curl --request POST \
  --url "https://api.trello.com/1/checklists" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idCard=<ID_KARTY>" \
  --data-urlencode "name=Implementacja"
```

### 4) Dodanie elementu checklisty

```bash
curl --request POST \
  --url "https://api.trello.com/1/checklists/<ID_CHECKLISTY>/checkItems" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "name=Uruchomic :app:compileDebugKotlin"
```

### 5) Oznaczenie elementu checklisty jako done

```bash
curl --request PUT \
  --url "https://api.trello.com/1/cards/<ID_KARTY>/checkItem/<ID_CHECKITEM>?key=${TRELLO_API_KEY}&token=${TRELLO_TOKEN}&state=complete"
```

### 6) Dodanie komentarza statusowego

```bash
curl --request POST \
  --url "https://api.trello.com/1/cards/<ID_KARTY>/actions/comments" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "text=STATUS: IN_PROGRESS | TASK: TASK-01 | NEXT: uruchomic compile"
```

### 7) Przeniesienie karty miedzy listami

```bash
curl --request PUT \
  --url "https://api.trello.com/1/cards/<ID_KARTY>" \
  --data-urlencode "key=${TRELLO_API_KEY}" \
  --data-urlencode "token=${TRELLO_TOKEN}" \
  --data-urlencode "idList=<ID_LISTY_REVIEW>"
```

## Konfiguracja agenta (krok po kroku)

### Krok 1: Wygeneruj klucze Trello

1. Wejdz na stronę deweloperska Trello i wygeneruj `API Key`.
2. Wygeneruj `Token` z uprawnieniami do odczytu i zapisu kart.
3. Zanotuj `Board ID` tablicy projektu.

### Krok 2: Ustaw zmienne srodowiskowe

Przyklad (PowerShell):

```powershell
$env:TRELLO_API_KEY="<twoj_api_key>"
$env:TRELLO_TOKEN="<twoj_token>"
$env:TRELLO_BOARD_ID="<twoj_board_id>"
```

Przyklad (bash):

```bash
export TRELLO_API_KEY="<twoj_api_key>"
export TRELLO_TOKEN="<twoj_token>"
export TRELLO_BOARD_ID="<twoj_board_id>"
```

### Krok 3: Zweryfikuj dostep

```bash
curl "https://api.trello.com/1/members/me?key=${TRELLO_API_KEY}&token=${TRELLO_TOKEN}"
```

Jesli odpowiedz zawiera dane uzytkownika, agent jest poprawnie skonfigurowany.

### Krok 4: Skonfiguruj workflow list

Upewnij sie, ze tablica ma listy:
- `Backlog`
- `Ready`
- `In Progress`
- `Review`
- `Done`

## Jak uzywac skilla w praktyce

### Scenariusz A: Utworzenie kart z dokumentu taskow

1. Agent czyta `docs/AI_IMPLEMENTATION_TASKS.md`.
2. Dla kazdego `TASK-*` tworzy 1 karte w `Backlog`.
3. W opisie przenosi: `Cel`, `Zakres`, `Kroki`, `Kryteria akceptacji`, `Ryzyka`.
4. Tworzy checkliste mapujaca kroki implementacyjne 1:1.

### Scenariusz B: Aktualizacja statusu po iteracji

Po kazdej iteracji agent:
1. Odhacza wykonane elementy checklisty.
2. Dodaje komentarz w formacie `STATUS: ...`.
3. Jesli kryteria spelnione - przenosi karte do `Review` lub `Done`.

### Scenariusz C: Domkniecie zadania

1. Agent potwierdza, ze wszystkie punkty checklisty sa complete.
2. Dodaje komentarz koncowy `STATUS: DONE ...` z informacja jak zweryfikowac efekt.
3. Przenosi karte do `Done`.

## Szybki prompt dla agenta

Uzyj tego polecenia, aby agent pracowal wg skilla:

```text
Uzyj wytycznych z docs/TRELLO_AGENT_SKILL.md. Wczytaj taski z docs/AI_IMPLEMENTATION_TASKS.md, utworz/uzupelnij karty na Trello, dodaj checklisty i aktualizuj status po kazdym kroku implementacji.
```

## Uwagi praktyczne

- Nie duplikowac kart dla tego samego zadania.
- Jeden task techniczny = jedna karta.
- Jezeli task jest duzy, tworz karty podrzedne przez prefiks, np. `TASK-05.1`, `TASK-05.2`.
