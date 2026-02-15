# Przeglad dlugu technologicznego

Data: 2026-02-15

## Zakres

Przeglad obejmuje:
- warstwe build/dependency (`build.gradle`, `app/build.gradle`, `gradle.properties`),
- warstwe UI po migracjach Compose (`app/src/main/java/com/example/internetapi/ui`),
- sygnaly jakosciowe (ostrzezenia kompilacji, TODO, testy).

## TL;DR

Najwiekszy dlug jest w 4 obszarach:
1. **Toolchain i zaleznosci sa przestarzale i kruche** (JDK/AGP/Kotlin/Compose + kapt workaround).
2. **Niska testowalnosc i brak testow regresyjnych** (w praktyce brak realnych testow).
3. **Niespojne wzorce stanu i side-effectow w Compose + LiveData** (duzo boilerplate i ryzyko bledow).
4. **Nawigacja i kontrakty Intenta oparte o stringi + Serializable (deprecated)**.

## Wnioski szczegolowe

| Priorytet | Obszar | Problem | Dowody | Wplyw | Pracochlonnosc | Zlozonosc |
|---|---|---|---|---|---|---|
| P1 | Build/toolchain | Niezgodnosc JDK 21 i stary stack AGP/Kotlin; wymuszone `kapt` exports | `gradle.properties:10-14`, `build.gradle:3-6`, `app/build.gradle:10,47-53` | Niestabilny build, koszt utrzymania rosnacy przy kazdej aktualizacji srodowiska | 2-4 dni | Srednia |
| P1 | Zaleznosci | Duplikaty i niespojne wersje bibliotek (np. Material dodany wielokrotnie, stare lifecycle/coroutines) | `app/build.gradle:58,72,75,82,78-79,92` | Trudniejsze debugowanie, konflikty, wolniejsze aktualizacje | 1-2 dni | Niska/Srednia |
| P1 | Testy | Praktyczny brak testow (tylko szablony) | `app/src/test/.../ExampleUnitTest.kt`, `app/src/androidTest/.../ExampleInstrumentedTest.kt` | Wysokie ryzyko regresji przy dalszych migracjach | 3-6 dni (MVP) | Srednia |
| P2 | Stan UI/Compose | Powtarzalny boilerplate `LiveData.observe + DisposableEffect` i reczne klucze refresh | liczne ekrany w `ui/*.kt` (np. `AccountOutcomeRegisterActivity.kt`, `AccountDetailsActivity.kt`) | Wysoki koszt zmian, latwo o niespojne zachowania | 3-5 dni | Srednia/Wysoka |
| P2 | Side effects | Czesci operacji mutujacych bez spójnej obserwacji wyniku (fire-and-forget / brak jednolitego feedbacku) | np. `AccountViewModel.addIncome()` zwraca `Unit` (`AccountViewModel.kt:41-44`) | Ryzyko "cichych" bledow i niespojnego UX | 2-4 dni | Srednia |
| P2 | Kontrakty nawigacji | `Intent` extras po stringach i `Serializable` (deprecated) | `AccountActivity.kt:129`, `BudgetActivity.kt:137`, `AccountUpdateActivity.kt:55`, `UpdateBudgetActivity.kt:63` | Krucha komunikacja miedzy ekranami, ryzyko runtime cast error | 2-3 dni | Srednia |
| P3 | Deprecated API | Uzycie API oznaczonych jako deprecated | warningi: `Invoice.kt:16`, `ChartActivity.kt:72-73`, adaptery (`adapterPosition`, `setColorFilter`) | Nieblokujace teraz, ale podnosi koszt aktualizacji | 1-2 dni | Niska |
| P3 | Jakosc kodu | Literowki i niespojne komunikaty/jezyki (PL/EN) | np. "Faile to revmoce", "Invaliad" w UI | Gorszy UX i utrudniona internacjonalizacja | 0.5-1 dnia | Niska |
| P3 | Konfiguracja Android | Namespace nadal z `AndroidManifest package` (ostrzezenie AGP) | warning przy build + `AndroidManifest.xml:3` | Niewielki dzis, ale wymagany przy modernizacji AGP | 0.5 dnia | Niska |

## Proponowany plan redukcji dlugu

### Etap 1 (najwiekszy zwrot, niski/umiarkowany koszt)
1. Uporzadkowac zaleznosci i wersje w `app/build.gradle` (usunac duplikaty, ujednolicic Material/Lifecycle/Coroutines).
2. Ustalic wspierana wersje JDK (17) i zaktualizowac dokumentacje + konfiguracje CI/IDE.
3. Wprowadzic namespace w gradle i docelowo usunac zaleznosc od `package` w manifeście.

**Szacowany koszt:** 2-4 dni, **zlozonosc:** srednia.

### Etap 2 (stabilnosc funkcjonalna)
1. Wprowadzic minimalny pakiet testow:
   - 5-10 testow jednostkowych dla formatterow/mapowan/request builderow,
   - 3-5 testow integracyjnych ViewModel (mock repo),
   - 1-2 krytyczne testy UI (happy path) dla ekranow po migracji.

**Szacowany koszt:** 3-6 dni, **zlozonosc:** srednia.

### Etap 3 (architektura UI)
1. Ujednolicic wzorzec stanu (np. `UiState + UiEvent`) i ograniczyc reczne `observe` w Composable.
2. Dla operacji zapisu/przenoszenia/usuwania zwracac jawny wynik i obslugiwac statusy w jednym miejscu.
3. Stopniowo zastapic `Serializable` bezpieczniejszym kontraktem (Parcelable/typed arguments).

**Szacowany koszt:** 5-10 dni (iteracyjnie), **zlozonosc:** srednia/wysoka.

## Rekomendowana kolejnosc prac

1. **Build/dependencies cleanup** (szybki zysk, odblokowuje dalsze zmiany).
2. **Pakiet testow regresyjnych** dla krytycznych flow.
3. **Refaktoryzacja wzorcow stanu i efektow ubocznych** w Compose.
4. **Migracja kontraktow Intenta** z `Serializable` na bezpieczniejsze rozwiazania.

## Ocena ogolna

- **Poziom dlugu technologicznego:** sredni-wysoki.
- **Ryzyko krótkoterminowe:** srednie (aplikacja sie buduje, ale jest krucha przy zmianach).
- **Ryzyko srednioterminowe:** wysokie bez testow i porzadkow w toolchainie.
- **Priorytet biznesowy:** wysoki dla stabilizacji, zanim dojdzie wiecej funkcji.
