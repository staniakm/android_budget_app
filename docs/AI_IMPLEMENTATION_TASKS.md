# Propozycja zmian - zadania gotowe do implementacji przez agenta AI

Data: 2026-02-15
Powiazanie: `docs/TECH_DEBT_REVIEW.md`

## Jak czytac ten dokument

Kazde zadanie zawiera:
- **Cel** (co i po co),
- **Zakres** (co wolno zmieniac),
- **Kroki implementacyjne** (kolejnosc prac),
- **Kryteria akceptacji** (warunki done),
- **Ryzyka i uwagi** (na co uwazac).

To jest format "AI-ready": agent powinien byc w stanie wykonac zadanie bez doprecyzowan biznesowych.

---

## TASK-01: Uporzadkowanie zaleznosci i duplikatow Gradle (P1)

**Status:** DONE

### Cel
Ujednolicic zaleznosci i usunac duplikaty, aby zmniejszyc ryzyko konfliktow i ulatwic aktualizacje.

### Zakres
- `app/build.gradle`
- opcjonalnie `build.gradle` (jezeli konieczne dla spojnosci wersji)

### Kroki implementacyjne
1. Zinwentaryzuj zaleznosci zdublowane i niespojne wersje.
2. Usun duplikaty (`material`, `lifecycle-viewmodel-ktx` itp.).
3. Ujednolic wersje bibliotek AndroidX/Material/Lifecycle/Coroutines do wzajemnie kompatybilnych.
4. Zachowaj zgodnosc z obecnym AGP/Kotlin (bez duzej migracji toolchain w tym tasku).
5. Uruchom kompilacje: `./gradlew :app:compileDebugKotlin`.

### Status realizacji (iteracyjnie)
- [x] Krok 1: zinwentaryzowano duplikaty i zaleznosci potencjalnie nieuzywane.
- [x] Krok 2 (iteracja 1): usunieto duplikaty `material` i `lifecycle-viewmodel-ktx`, usunieto `android.arch.lifecycle:extensions`.
- [x] Krok 2 (iteracja 2): usunieto nieuzywane zaleznosci `navigation-*` oraz `fragment-ktx`; usunieto tez martwy zasob `res/navigation/nav_graph.xml` (stary szablon First/SecondFragment).
- [x] Krok 3: wykonano bezpieczne ujednolicenie wersji (Coroutines/Activity/Lifecycle) bez migracji AGP/Kotlin.
- [x] Krok 4: zachowano zgodnosc z obecnym AGP/Kotlin (bez migracji toolchain).
- [x] Krok 5: kompilacja `:app:compileDebugKotlin` przechodzi po kazdej iteracji.

### Kryteria akceptacji
- Brak duplikatow dependencies w `app/build.gradle`.
- Build `:app:compileDebugKotlin` przechodzi.
- Nie pogorszono funkcjonalnosci aplikacji (brak nowych bledow kompilacji/runtime wynikajacych ze zmian wersji).

### Ryzyka i uwagi
- Nie wykonywac duzej aktualizacji AGP/Kotlin w tym zadaniu.
- Jesli zmiana jednej biblioteki wymaga aktualizacji innej, zanotowac to w komentarzu PR.

---

## TASK-02: Standaryzacja JDK 17 w projekcie (P1)

### Cel
Wyeliminowac niestabilnosc builda wynikajaca z uruchamiania Gradle na niewspieranym JDK.

### Zakres
- dokumentacja projektu (README lub `docs/`)
- konfiguracja build (tylko jesli konieczne i bezpieczne)

### Kroki implementacyjne
1. Dodaj sekcje "Wymagania srodowiskowe" z jednoznacznym wskazaniem JDK 17.
2. Opisz szybka procedure weryfikacji (`java -version`, `./gradlew -version`).
3. Jesli repo posiada workflow CI, ustaw Java 17 i zweryfikuj syntax workflow.
4. Sprawdz kompilacje lokalnie na JDK 17: `./gradlew :app:compileDebugKotlin`.

### Kryteria akceptacji
- W dokumentacji jest jasna informacja: build wspiera JDK 17.
- Agent jest w stanie odtworzyc build zgodnie z instrukcja.

### Ryzyka i uwagi
- Nie usuwac na sile workaroundow `kapt.jvmargs`, jesli to destabilizuje build.

---

## TASK-03: Migracja namespace z manifestu do Gradle (P3)

### Cel
Usunac ostrzezenie AGP dot. `package` w AndroidManifest i dostosowac projekt do nowoczesnej konfiguracji.

### Zakres
- `app/build.gradle`
- `app/src/main/AndroidManifest.xml`

### Kroki implementacyjne
1. Dodaj `namespace "com.example.internetapi"` w module `app`.
2. Zweryfikuj czy `applicationId` pozostaje bez zmian.
3. Usun/zmodyfikuj `package` w manifeście zgodnie z wymaganiami AGP.
4. Uruchom: `./gradlew :app:processDebugMainManifest :app:compileDebugKotlin`.

### Kryteria akceptacji
- Ostrzezenie o namespace znika lub jest istotnie ograniczone.
- Build przechodzi.

### Ryzyka i uwagi
- Nie zmieniac package names w kodzie Kotlin/Java.

---

## TASK-04: Minimalny pakiet testow regresyjnych (P1)

### Cel
Zbudowac podstawowa siatke bezpieczenstwa przed regresjami po migracjach Compose.

### Zakres
- `app/src/test/...`
- `app/src/androidTest/...` (minimalnie)

### Kroki implementacyjne
1. Dodaj testy jednostkowe dla formatterow i transformacji requestow:
   - Money/Amount formatting,
   - mapowanie danych wejscia do requestow (np. invoice request).
2. Dodaj testy ViewModel z mockami repozytoriow (minimum 3 krytyczne przypadki sukces/error).
3. Dodaj 1-2 testy UI/instrumentacyjne dla krytycznego flow (np. zapis faktury).
4. Dodaj instrukcje uruchomienia testow do dokumentacji.

### Kryteria akceptacji
- Testy uruchamiaja sie lokalnie.
- Pokryte sa minimum: happy path + error path dla 2 najwazniejszych flow.

### Ryzyka i uwagi
- Nie celowac w wysokie pokrycie procentowe w tym kroku - to ma byc "MVP safety net".

---

## TASK-05: Ujednolicenie wzorca stanu UI w Compose (P2)

### Cel
Ograniczyc boilerplate i niespojnosci w ekranach Compose obserwujacych LiveData.

### Zakres
- wybrane ekrany w `app/src/main/java/com/example/internetapi/ui`
- nowy pomocniczy modul/plik z konwencja stanu (jezeli potrzebny)

### Kroki implementacyjne
1. Zdefiniuj jednolity wzorzec (`UiState` + `UiEvent` lub inny spójny pattern).
2. Wybierz 1 ekran pilotażowy (np. `AccountOutcomeDetails`) i wdroz wzorzec.
3. Upewnij sie, ze loading/error/success sa obslugiwane identycznie.
4. Rozszerz wzorzec na kolejne 2-3 ekrany o podobnym charakterze.
5. Dodaj krotka dokumentacje konwencji w `docs/`.

### Kryteria akceptacji
- Na min. 3 ekranach nie ma rozproszonych, ad-hoc wzorcow side-effect.
- Kod jest krotszy lub czytelniejszy bez utraty funkcjonalnosci.

### Ryzyka i uwagi
- Nie robic "big bang" na wszystkie ekrany naraz.

---

## TASK-06: Ujednolicenie obslugi operacji mutujacych (P2)

### Cel
Zapewnic przewidywalny feedback dla operacji add/update/delete.

### Zakres
- ViewModel + UI dla flow mutujacych (konto, faktury, media, budzety)

### Kroki implementacyjne
1. Zinwentaryzuj metody fire-and-forget (np. metody zwracajace `Unit`).
2. Zmien kontrakty tak, aby operacje zwracaly obserwowalny wynik (`Resource`/status).
3. W UI kazdego flow dodaj jawna obsluge: loading, success, error.
4. Ujednolic komunikaty snackbara i moment ich pokazywania.

### Kryteria akceptacji
- Kazda operacja mutujaca ma widoczny wynik dla uzytkownika.
- Brak "cichych" bledow bez feedbacku.

### Ryzyka i uwagi
- Zmiany kontraktow moga wymusic edycje wielu ekranow - robic etapami.

---

## TASK-07: Migracja kontraktow Intenta z Serializable (P2)

### Cel
Usunac deprecated API i zmniejszyc ryzyko runtime cast errors.

### Zakres
- Activity kontrakty (`Intent extras`) w `ui/*`
- modele przekazywane miedzy ekranami

### Kroki implementacyjne
1. Zlokalizuj wszystkie `getSerializable()` i `getSerializableExtra()`.
2. Zdecyduj strategia: `Parcelable` lub przekazywanie ID + dociaganie danych.
3. Zmigruj najpierw krytyczne flow:
   - Account update,
   - Budget update,
   - inne miejsca z warningami.
4. Dodaj helpery/extension functions dla bezpiecznego odczytu extras.
5. Zweryfikuj build i recznie przetestuj nawigacje.

### Kryteria akceptacji
- Brak uzyc deprecated `getSerializable*` w krytycznych flow.
- Nawigacja dziala bez cast warningow.

### Ryzyka i uwagi
- Parcelable moze wymagac zmian modeli i potencjalnie kompatybilnosci wstecz.

---

## TASK-08: Cleanup deprecated API i warningow kompilacji (P3)

### Cel
Zredukowac liczbe warningow i przygotowac kod pod dalsze aktualizacje.

### Zakres
- pliki raportowane przez kompilator

### Kroki implementacyjne
1. Zamien `toUpperCase(Locale.ROOT)` na `uppercase(Locale.ROOT)`.
2. Zmien deprecated uzycia API UI (`adapterPosition`, `setColorFilter`, inne wskazane warningi).
3. Przejrzyj warningi po kompilacji i usuń te o niskim ryzyku.

### Kryteria akceptacji
- Istotna redukcja warningow kompilatora.
- Brak regresji funkcjonalnej.

### Ryzyka i uwagi
- Nie wszystkie warningi trzeba zamykac w jednym PR - dopuszczalny rollout etapowy.

---

## TASK-09: Ujednolicenie tekstow i poprawa literowek (P3)

### Cel
Poprawic jakosc UX i przygotowac grunt pod internacjonalizacje.

### Zakres
- `strings.xml`
- hardcoded stringi w ekranach Compose

### Kroki implementacyjne
1. Znajdz hardcoded teksty w `ui/*.kt`.
2. Przenies je do `strings.xml`.
3. Popraw literowki i niespojne komunikaty (PL/EN).
4. Sprawdz, czy nowe string resources sa uzyte wszedzie zamiast literalow.

### Kryteria akceptacji
- Brak nowych hardcoded stringow w modyfikowanych ekranach.
- Poprawione literowki i spojny jezyk komunikatow.

### Ryzyka i uwagi
- Jesli finalnie app ma byc dwujezyczna, zaplanowac osobne taski na `values-pl` / `values-en`.

---

## Sugestia kolejnosci realizacji

1. TASK-01
2. TASK-02
3. TASK-04
4. TASK-06
5. TASK-07
6. TASK-08
7. TASK-09
8. TASK-03
9. TASK-05

Uwaga: TASK-03 i TASK-05 moga byc przesuniete zaleznie od tempa prac nad stabilizacja i testami.
