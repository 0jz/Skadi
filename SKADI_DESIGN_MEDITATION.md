# Skadi — Design & Implementation Spec (Meditation maska)

Build spec za Android (Kotlin + Jetpack Compose). Za product kontekst vidi `SKADI.md`. Ovo je varijanta sa **meditacijom kao decoy** (umesto trackera vode). Ostatak arhitekture je identičan; menja se samo cover ekran i okidač.

**Cilj MVP-a (do roka):** decoy meditation app radi kao prava → tajni okidač otvara skriveni sloj → skener (rezultati spremni unapred) → izbor Leči/Seči, pri čemu se Seči dešava samo na deliberativan, gate-ovan izbor.

**Stack:** single-Activity, Jetpack Compose, Kotlin, minSdk 26+ / target 35. Bez backenda. Bez root-a. Persistentno = samo decoy podaci (sesije meditacije); nalazi skenera **nikad ne idu na disk**.

**Predlog naziva maske:** „Smiraj" / „Predah" / „Dah" (srpski deluje uverljivije od engleskog Calm klona).

---

## 0. Redosled gradnje (POČNI ODAVDE — radi fazno)

Gradi se **cover prvo, pa tajni sloj.** Ne skeletiraj tajni deo dok decoy nije gotov i uverljiv. Ako cover nije 100% funkcionalan, koncept pada.

### Faza 1 — Decoy (prvo, mora biti potpuno gotovo)
- Breathing krug (animacija udah-izdah), timer sesije sa presetima (3/5/10/15 min) + **custom polje**, dugme „Počni".
- Tabovi Meditiraj / Istorija / Podešavanja; istorija sa streak-om; **persistencija sesija** (Room/DataStore).
- Ambient zvuk opciono.
- Rezultat: app se pokreće, radi, izgleda kao prava meditacija — **bez ijednog traga tajnog dela.**

### Faza 2 — Skela tajnog sloja (tek kad Faza 1 radi)
- `Screen` enum + state holder; magic value okidač u custom timer polju → **prazan** Diagnostics ekran.
- `activity-alias` (maska „Smiraj") se postavlja ovde.
- `FLAG_SECURE` + panic-exit (`onStop` reset) — ugradi **čim** postoji prvi tajni ekran, ne kasnije.

### Faza 3 — Skener + Safety
- Pozadinski sken u in-memory `ScanSnapshot`, Diagnostics lista, Safety gate, Leči/Seči.

> **Kritično za izvođača:** u Fazi 1 napiši custom timer unos tako da prolazi kroz jednu callback funkciju (npr. `onCustomDurationEntered(value)`). U Fazi 2 se magic value provera **dodaje u tu funkciju** — tako Faza 1 ostaje čista meditacija, a tajni sloj je mali izolovan dodatak, ne naknadna prepravka cover-a.

---

## 1. Princip dizajna (ne pregaziti ovo)

1. **Nema vidljivih vrata.** Okidač je vrednost koju korisnik unese, ne tajni UI element.
2. **Graceful degradation.** Tajni sloj se otkriva u 3 nivoa; opasan sadržaj (DV jezik, SOS brojevi) iza poslednjeg gate-a.
3. **Skeniranje unapred.** Skener radi u pozadini, rezultati spremni pre nego što korisnik uđe.
4. **Rez je uvek odvojen, deliberativan korak.** Nikad auto, nikad one-tap.
5. **Panic-izlaz instant i bez traga.**

### Zašto meditacija dobro funkcioniše kao maska
- Nizak snoop-appeal (partner pre proverava poruke/kalorije nego koliko medituje).
- Privacy/permissions ekran (Nivo 2) deluje **prirodnije** nego kod vode — meditation aplikacije legitimno traže mikrofon (disanje), Health podatke, lokaciju (ambient zvuci).
- Duga sesija = pokriće: otvori timer od 10 min, spusti telefon, ekran legitimno stoji.

---

## 2. Tri nivoa izloženosti (mentalni model za ceo UI)

| Nivo | Šta napadač vidi ako dođe dovde | Sadržaj |
|---|---|---|
| **1 — Cover** | Aplikacija za meditaciju | Pun, funkcionalan timer/sesije |
| **2 — Diagnostics** | „Privatnost i dozvole" — bezazlen wellness feature | Skener dozvola/lokacije, neutralan jezik, **bez** reči nasilnik/stalkerware/SOS |
| **3 — Safety** | (samo iza „jesi na bezbednom mestu?") | Objašnjeni nalazi, Leči/Seči, ASTRA brojevi, evidence |

Pun „otkrije" kod napadača zastaje na nivou 2 — izgleda kao privacy checker (a meditation aplikacije ionako imaju takav meni), ne kao safety vault.

---

## 3. Ekrani

### 3.1 Meditation (Nivo 1 — cover)
- Centralni **breathing krug** koji se širi/skuplja (animacija udah-izdah).
- **Timer sesije** sa preset dužinama (3 / 5 / 10 / 15 min) + **„Prilagodi" (custom minute)** polje. ← custom polje je okidač (§4).
- Dugme „Počni".
- Donji tabovi: **Meditiraj / Istorija / Podešavanja**.
- Istorija: kalendar/graf sesija + **streak (dani zaredom)** (alternativni okidač, §4).
- Opcioni izbor ambient zvuka (kiša/šuma) — pojačava uverljivost.
- Sve radi i persistira (Room/DataStore) — mora da izdrži 10s tuđeg skrolovanja.

### 3.2 Diagnostics (Nivo 2)
- Naslov: „Privatnost i dozvole". Podnaslov: „Pogledaj koje aplikacije pristupaju tvojoj lokaciji, mikrofonu i zdravstvenim podacima."
- Lista nalaza iz skenera, **neutralno**: tier badge (crveno/žuto/zeleno), ime app-a, koje dozvole, kratko objašnjenje. Nema DV jezika.
- Ako skan još traje: neutralan progress „Proveravam podešavanja privatnosti…".
- Jedan primarni CTA dublje — koji **otvara gate** (§3.3), ne odmah Safety.

### 3.3 Safety gate
- Pun-screen, mirno: „Jesi li na bezbednom mestu? Može li te neko sada videti?" + „Imaš li kuda ako situacija eskalira?"
- Potvrda → Safety. Odustani → nazad na Diagnostics (i dalje izgleda kao privacy feature).

### 3.4 Safety (Nivo 3)
- Nalazi objašnjeni ljudskim jezikom + šta znače.
- **Toggle Leči / Seči.**
  - **Leči:** mapa sa dva pina (tačna vs nivo kvarta — coarsening), „ništa se ne menja vidljivo", tihi snapshot u evidence (in-memory).
  - **Seči:** checklist bezbednog redosleda (dokumentuj → re-secure nalozi → revoke sesije → ukloni trackere → uskladi sa ASTRA-om), generator lozinki, deep-link na Google Password Checkup. **Svaka destruktivna akcija je poseban tap, nikad batch one-tap.**
- ASTRA i SOS brojevi (tap-to-call) — postoje samo na ovom ekranu.

---

## 4. Okidač

**Primarni (preporučeno): magic value u custom timer polju.**
- Korisnik u „Prilagodi" unese tajnu dužinu sesije (npr. `0`, `99`, ili `4:44`) → umesto pokretanja meditacije, prelaz na Diagnostics.
- Broj bira korisnik u onboardingu; default = vrednost van realnog opsega (0 ili >60 min) da se nikad ne sudari sa pravom sesijom.
- Zašto: prirodno numeričko polje (ekvivalent ml polja kod vode), nigde nema tajnog dugmeta/gesta, ~2s, slučajno nemoguće.

**Alternativa (ako se hoće gest): long-press na streak brojač** (`≥1.5s`) u Istoriji. Streak je prirodno „tapni za detalje", pa dug pritisak deluje kao feature.

> Implementiraj **jedan** od ova dva, ne oba. Magic value (custom timer) je default.
> Izbegavaj gest na breathing krugu — lako se slučajno okine i lakše se otkrije.

---

## 5. State model & navigacija

Single-Activity, jedan top-level state:

```kotlin
enum class Screen { Meditation, Diagnostics, SafetyGate, Safety }
```

- Compose `when(screen)` render, bez deep back-stack-a ka tajnom sloju (`back` uvek vodi nazad ka Meditation, ne među tajne ekrane).
- Prelaz na tajni sloj se drži kao in-memory state, ne kroz sistemski Navigation back-stack.

### Skener (rezultati unapred, samo RAM)
```kotlin
data class ScanSnapshot(val findings: List<Finding>, val ranAt: Long)
// drži se u ViewModel / process-scoped singleton, NIKAD Room/DataStore/file
```
- Pokreće se oportunistički (coroutine na app start / WorkManager dok je na punjaču) i puni `ScanSnapshot`.
- Opcioni „Pripremi proveru" dugme u Diagnostics da korisnik ručno pokrene pun audit kad je bezbedna.
- Ako je proces ubijen → snapshot prazan → re-sken na ulazu uz neutralan progress.
- Evidence/Leči snapshot takođe **samo in-memory** — nikad ne zapisuj „detektovan stalkerware" na disk (honeypot rizik).

---

## 6. Panic-izlaz & maskiranje (obavezno)

- **Instant exit:** hardverski `back`, tap van, ili app u pozadini dok je u Diagnostics/Safety → odmah `screen = Meditation` **i reset tajnog state-a** (ponovni ulazak pokazuje meditaciju, ne gde je stala).
- `Activity.onStop()` u tajnom sloju → reset na Meditation.
- **`FLAG_SECURE`** se pali na ulazu u Diagnostics/Safety, gasi u Meditation → recents/screenshot ne pokazuju ništa osim meditacije.
- **Exclude-from-recents** na tajnim ekranima.
- **`activity-alias`** drži ime „Smiraj" + bezazlenu ikonu u launcheru (cover identitet).

---

## 7. Skener — šta da čita (bez root-a)

- `PackageManager.getInstalledApplications` + `getPackageInfo(pkg, GET_PERMISSIONS)` → markiraj opasnu kombinaciju (lokacija + mikrofon + SMS + pozivi + kamera).
- `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`, `ENABLED_NOTIFICATION_LISTENERS`, Usage Access, `DevicePolicyManager.getActiveAdmins()` → 4 „moćne" dozvole.
- App-ovi bez launcher intent-a → „instaliran, bez ikone".
- IOC match: bundluj **Coalition Against Stalkerware** listu (`stalkerware-indicators`) kao lokalni JSON, uporedi imena paketa.
- **Ne čitaj lozinke.** Za izloženost: deep-link na Google Password Checkup; HaveIBeenPwned (k-anonymity) za email. Seči **generiše** nove lozinke, ne čita postojeće.

---

## 8. MVP rez (ako vreme gori)

Mora da klikće cela spina: **Meditation → okidač → Diagnostics → gate → Safety → Leči/Seči izbor.**
Ako Kotlin skener kasni: prikaži skan kao „u toku" sa jednim **stvarnim** nalazom (sideloadovana lažna „System Update" app sa scary kombinacijom dozvola + na IOC listi) — demo wow-moment. Sve ostalo (AirGuard/BLE, PDF export, sesija-audit) ide kao roadmap, ne gradi se sada.

---

## 9. Predlog strukture paketa

```
com.smiraj.meditation
├── MainActivity.kt              // single Activity, FLAG_SECURE toggle, onStop reset
├── AppState.kt                  // Screen enum, top-level state holder
├── meditation/                  // cover: MeditationScreen, breathing krug, timer + custom (okidač), History, Settings
├── diagnostics/                 // Nivo 2: DiagnosticsScreen, neutralan prikaz nalaza
├── safety/                      // Nivo 3: SafetyGate, SafetyScreen, Leči, Seči
├── scan/                        // ScanSnapshot (in-memory), PackageScanner, IOC matcher
│   └── ioc.json                 // bundlovana stalkerware-indicators lista
├── location/                    // coarsening (jitter unutar radijusa)
└── resources/                   // ASTRA brojevi, tap-to-call
```

---

## 10. Napomena za izvođača (Cowork)

- Nikad ne zapisuj nalaze skenera, evidence, ni „secret mode" zastavicu u persistentni storage. Jedini persistentni podaci su sesije meditacije.
- Nikad ne pravi vidljiv „secret menu" element u Meditation UI-ju. Okidač je magic value u custom timer polju (ili long-press na streak, ne oba).
- Neutralan jezik na Nivou 2; DV jezik i SOS brojevi tek na Nivou 3 iza gate-a.
- Maska nije „nevidljivost" — paket i dalje postoji u Podešavanjima; ne pokušavaj potpuno skrivanje (stalkerware pattern, Play ga flaguje).
- Breathing animacija i ambient zvuk nisu „nice-to-have" — oni nose uverljivost cover-a; neka rade pre nego što se polira tajni sloj.
