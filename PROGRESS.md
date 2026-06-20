# Skadi - Dnevnik napretka

Poslednje azurirano: 20. jun 2026.

## Trenutno stanje

Radimo na branchu `secret`. Branch `secret` je krenuo sa istog commita kao `main`
(`9842b98 decoy app`), dok je stari `feature/secret-diagnostics` ostavljen po
strani jer u istoriji ima zagadjen commit sa build artefaktima i brisanjem
source fajlova.

## 1. Faza 1 - Decoy "Smiraj" (uradjeno)

Smiraj je funkcionalna meditation cover aplikacija:

- Meditiraj tab: breathing krug, preseti 3/5/10/15 min, custom polje,
  Pocni/Zaustavi, odbrojavanje.
- Istorija tab: streak, broj sesija, ukupno minuta, lista sesija.
- Podesavanja tab: izbor ambijentalnog zvuka.
- Room cuva samo sesije meditacije; DataStore cuva samo cover podesavanja.

Kljuccna tacka ostaje `AppViewModel.onCustomDurationEntered()`: sav custom unos
ide kroz jednu funkciju, pa tajni okidac ne dira cover UI.

## 2. Faza 2 - Secret/Diagnostics sloj (vraceno na `secret`)

Faza 2 je ponovo ozicena, cisto i bez oslanjanja na pokvaren branch:

- `Screen` enum: `Meditation`, `Diagnostics`, `SafetyGate`, `Safety`.
- Tajni kod je `AppViewModel.TRIGGER_CODE = 0`.
- Unos `0` u `Prilagodi` otvara `Diagnostics`, umesto da startuje sesiju.
- `FLAG_SECURE` se pali na tajnim ekranima i gasi na coveru.
- Panic izlaz: hardverski back i `onStop` vracaju aplikaciju na Meditation.
- Diagnostics koristi neutralan jezik: "Privatnost i dozvole", bez DV/SOS jezika.
- Podesavanja su trimovana: uklonjeni su "O aplikaciji" i screen-on toggle.

## 3. MVP prosirenje - Lecenje/Secenje (klikabilan spine)

Dodato je dovoljno da demo tok radi od pocetka do kraja:

1. Meditation cover.
2. Tajni kod `0`.
3. Diagnostics bez mock nalaza; realni skener se dodaje kroz posebne grane.
4. Safety gate: "Jesi li na bezbednom mestu?"
5. Safety ekran sa toggle-om `Leci` / `Seci`.

`Leci`:

- Prikazuje harm-reduction rezim.
- Ima placeholder za mapu: tacna tacka -> nivo kvarta.
- "Tihi snapshot" se oznacava samo u memoriji ViewModel-a.

`Seci`:

- Checklist bezbednog redosleda: dokumentuj, promeni lozinke, izbaci sesije,
  ukloni sumnjive aplikacije, uskladi tajming sa podrskom.
- Nema predloga nove lozinke i nema generisanja lozinke u clean shell-u.
- Lozinke i account audit idu u zaseban feature sloj.
- Tap-to-call za ASTRA i policiju.

Nema automatskog brisanja, nema batch akcija, nema cuvanja nalaza na disk.

## 4. Tehnicki plan za pravi skener

Sledeci sloj treba da zameni demo `ScanSnapshot` realnim in-memory skenerom:

- `scan/PackageScanner`: `PackageManager.getInstalledApplications()` +
  `getPackageInfo(..., GET_PERMISSIONS)`.
- Signal kombinacije: lokacija, mikrofon, SMS, pozivi, kamera.
- Mocne dozvole: Accessibility, Notification Listener, Device Admin, Usage Access.
- "Bez ikone": app bez launcher intenta.
- IOC matcher: lokalni JSON iz Coalition Against Stalkerware indikatora.
- Sve rezultate drzati u `ScanSnapshot` u ViewModel/process memoriji, nikad Room,
  DataStore ili fajl.

Za demo je dovoljno da imamo jedan stvaran nalaz iz posebne test fixture app.
Runtime mock nalazi ne treba da budu deo clean secret shell grane.

## 5. Build provera

`assembleDebug` je prosao preko lokalno preuzetog Gradle 8.9:

`C:\Users\nikol\.gradle\wrapper\dists\gradle-8.9-bin\...\gradle-8.9\bin\gradle.bat assembleDebug`

U repo-u nema `gradlew.bat` skripte, i sistemski `gradle` nije u PATH-u.

## 5a. Clean secret shell

Grana `integration/clean-secret-shell` je namenjena za spajanje realnih feature-a
bez runtime mock bezbednosnih nalaza. Tajni meni ostaje, ali `ScanSnapshot` se na
ulazu resetuje na prazan rezultat. Mock/test podaci treba da žive samo u
`feature/real-scanner-test-fixture` ili demo/debug flavor-u.

Clean shell takodje ne sadrzi Password Checkup, predloge novih lozinki ili
generator lozinki. Ti delovi treba da se vrate tek kroz
`feature/real-scanner-account-audit` i odgovarajuce `cut-*` grane, kada budu
vezani za pravila privatnosti, offline obradu i jasnu jacinu secenja.

## 6. Pitch deck kasnije

Kad se branch sredi i demo tok bude finalno ispoliran, deck treba da prati ovu
pricu:

- Problem: pracenje nije samo malware; detekcija bez plana moze da eskalira.
- Insight: svaka akcija menja pretnju.
- Demo: Smiraj cover -> tajni kod -> Diagnostics -> gate -> Leci/Seci.
- Diferencijacija: survivor-centered redosled, ne "one tap delete".
- Tehnicka iskrenost: native Android moze da cita dozvole; nema root-a, nema
  lozinki, nema cuvanja nalaza.
- Roadmap: AirGuard/BLE, realni account audit vodiči, evidence export, Sapta
  integracija.

## 7. Real scanner plan

Dodati su planovi za sledeci sloj, bez implementacije feature koda:

- `docs/REAL_SCANNER_EXECUTION_PLAN.md` - redosled branch-eva, Leči kao report,
  Seči jačine, demo vs production scope, privatnosna pravila.
- `docs/PITCH_DECK.md` - skeleton pitch deck-a.
- `docs/MENTOR_TECHNICAL_BRIEF.md` - kratko tehnicko objasnjenje za mentora:
  tehnologije, Leči/Seči metode, hitne situacije i granice scope-a.
- `docs/FEATURE_BRANCH_ROADMAP.md` - opis svake feature grane, redosled cetiri
  bezbednosna dela, redosled Seči jacina, test fixture/demo plan i merge pravila.

## 8. feature/real-scanner-heal-report (uradjeno)

Grana bazirana na `integration/clean-secret-shell`.

Konvertovana stara Safety screen u strukturirani Leči izveštaj sa četiri
sekcije. Sve sekcije su u "nije spreman / demo" stanju — realni podaci dolaze
kroz scanner i audit grane prema redosledu iz roadmapa.

### Šta je dodato

- `scan/PreflightResult.kt` — sealed class: `Clear`, `BlockedByAccessibilityRisk`,
  `NeedsGuidedAudit`.
- `scan/LeciReport.kt` — četiri data klase sekcija: `AppsSection`,
  `AccountsSection`, `LocationSection`, `DeviceSection`, plus pomoćni modeli
  `AccountEntry` i `DeviceCheckItem`. `LeciReport.demo()` factory za prazno
  početno stanje.
- `AppState.kt` — dodat `Screen.PreflightBlocked`.
- `AppViewModel.kt` — `confirmSafetyGate()` sada pokreće preflight pre otvaranja
  Safety ekrana. Ako `BlockedByAccessibilityRisk`, ide na `PreflightBlocked` umesto
  `Safety`. `runPreflight()` placeholder vraća `Clear` dok
  `feature/real-scanner-special-access` ne doda prave provere. Izbačen
  `healSnapshotPrepared` (ne treba više). `exitToCover()` briše i `_leciReport`.
- `safety/PreflightBlockedScreen.kt` — neutralan ekran bez DV jezika za slučaj
  blokiranja. Vraća na cover.
- `safety/SafetyScreen.kt` — potpuno repisana. Leči tab prikazuje četiri report
  kartice sa sekcijskim ikonama. Svaka kartica prikazuje placeholder tekst dok
  sekcija nije `ready`, ili realne podatke kada scanner grane popune model. Seči
  tab zadržava postojeći checklist (radi se kroz `cut-*` grane). `SeverityChip`
  helper za vizuelnu oznaku rizika.
- `MainActivity.kt` — dodat case za `Screen.PreflightBlocked`. `Screen.Safety`
  sada prima `LeciReport` umesto `ScanSnapshot` i `healSnapshotPrepared`.
- `strings.xml` — novi stringovi za preflight blocked, guided-audit baner, Leči
  intro, četiri sekcije i risk level oznake.

### Šta nije promennjeno

- `ScanSnapshot.kt` i `Finding` model — ostaju za Diagnostics screen i buduće
  scanner grane.
- `DiagnosticsScreen.kt` — bez promena.
- `SafetyGateScreen.kt` — bez promena.
- `SafetyMode.kt` — bez promena.

### Sledeće grane

`feature/real-scanner-app-scanner` popunjava `AppsSection` realnim PackageManager
podacima i `Finding` listom. Taj branch se merguje u `integration/real-scanner-main`
prema redosledu iz `docs/FEATURE_BRANCH_ROADMAP.md`.
