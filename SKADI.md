# Skadi

> Survivor-centered anti-tracking tool disguised as a water tracker — finds how an abuser may be monitoring you, then helps cut it off safely.

**SheSafe Hackathon — Izazov 03 (Tracking Check), Beograd, jun 2026.**
Žiri: ASTRA, ICT Hub, UN Women.

---

## 1. Problem

Tehnološki posredovano nasilje nad ženama (TF-VAWG): nasilnik prati žrtvu kroz njen telefon i naloge. Naivni „antivirus za špijunske aplikacije" promaši suštinu, jer:

- **Većina praćenja nije skrivena spyware aplikacija**, nego zloupotreba *legitimnog* pristupa (deljeni nalog, location sharing, Find My, Family Link, AirTag).
- **Detekcija bez plana je opasna.** Najopasniji trenutak u nasilnoj vezi je odlazak; naglo brisanje/izbacivanje signalizira nasilniku da žrtva zna → eskalacija.

Skadi je građen oko jedne ideje: **detekcija je lak deo — poenta je uraditi to bez povećanja opasnosti.**

---

## 2. Dva ključna uvida (diferencijacija za žiri)

1. **Praćenje ≠ malware.** Najčešći vektor je legitiman pristup koji nasilnik kontroliše. Skadi pokriva i taj vektor, ne samo APK skener.
2. **Svaka akcija menja pretnju.** Pozitivan nalaz je *safety event*, ne „one-tap delete". Zato: snimi → planiraj → tek onda deluj, uz bezbedan redosled.

---

## 3. Koncept

**Ime:** Skadi (nordijska boginja lova i zime — lov se okreće protiv lovca).

**Maska (decoy):** aplikacija za praćenje unosa vode — radi kao pravi tracker (upis ml, dnevni cilj, mali streak). Bira se voda jer ima **nizak snoop-appeal** (kontrolišući partner pre proverava kalorije/ciklus), nema bazu da se puni, ima prirodno numeričko polje za tajni kod, i pravi se uverljivo za pola dana.

**Okidač:** tajni kod ukucan u polje za ml otvara skriveni Skadi sloj. Nasilnik sa fizičkim pristupom telefonu vidi samo lifestyle aplikaciju.

### Iskrene granice (da nas žiri ne uhvati u Q&A)
- **Maska ≠ nevidljivost.** App može da zameni ikonu/ime na home screen-u i u recents-u, ali u *Podešavanja → Aplikacije* paket i dalje postoji (pod maskiranim imenom). App koji se potpuno sakrije bez ikone = stalkerware pattern; Play to flaguje. Iskren framing: „izgleda kao tracker vode, ne upada u oči" — ne „nevidljiv je".
- **Decoy štiti alat, ne posledice.** Maska skriva Skadi na telefonu, ali Seči akcije prave spoljašnje signale koje nasilnik vidi bez obzira na masku.

---

## 4. Dva moda: Leči i Seči

| | **Leči (Heal)** | **Seči (Cut)** |
|---|---|---|
| Kada | Žrtva je još u vezi, kupuje vreme | Exit moment — spremna da ode |
| Cilj | Harm reduction, **ništa vidljivo se ne menja** | Puno čišćenje, ali isplanirano |
| Ključno | Nasilnik ništa ne primeti | Nasilnik **će** primetiti → mora biti planirano |
| Gate | — | Iza provere „jesi na bezbednom mestu? imaš kuda?" |

**Leči** uključuje coarsening GPS-a (vidi sekciju 6) i tihi snapshot nalaza u evidence vault. Nema brisanja, nema promene lozinki — samo priprema.

**Seči** je bezbedan redosled (vidi sekciju 7).

---

## 5. Skener — šta proverava (4 kategorije)

Skadi je threat model napadača pretvoren u checklistu. Za svaki napadačev potez — jedna stavka audita.

1. **Nalozi (najčešće).** „Gde si ulogovana" (Google Security → uređaji/sesije; Apple → lista uređaja), recovery email/broj („da li je *tvoj*?"), 2FA status, third-party app pristup, deljeni cloud backup.
2. **Deljenje lokacije (legitimno).** Google Maps location sharing, Find My / Share My Location, Find My Device, Family Link/Sharing (ko je organizator), nepoznati Bluetooth trackeri (AirTag/Tile), geotag na deljenim slikama.
3. **Stalkerware app.** Opasna kombinacija dozvola (lokacija + mikrofon + SMS + pozivi + kamera), 4 „moćne" dozvole (Accessibility / Notification Listener / Device Admin / Usage Access), skriveni app-ovi bez ikone, IOC match na otvorenoj listi **Coalition Against Stalkerware** (Echap `stalkerware-indicators`), status Play Protect-a.
4. **Uređaj / fizički.** Upisane biometrije (drugi otisak/lice), uparени uređaji (smartwatch), nosilac pretplate kod operatera, browser sync.

### Šta skener NE radi (kritično)
- **Ne čita lozinke.** Google saved passwords su sandbox-ovani — čita ih samo OS autofill. App koja čita tuđe lozinke je *bukvalno* stalkerware. Umesto toga:
  - **Izloženost, ne sadržaj:** email kroz HaveIBeenPwned (k-anonymity), deep-link na Google Password Checkup / iOS Security Recommendations (OS sme, mi ne).
  - **Seči generiše** jake nove lozinke (korisnica kopira) i **pokazuje za koju app je lozinka slaba/procurela** preko Password Checkup-a — ne čita postojeće.
- **Ne drži podatke.** Glavna i sporedne lokacije se unose u app, drže se **samo u RAM-u** dok je otvorena, ništa na disk.
- **Ne koristi draw-over-other-apps za nadzor.** Iako su organizatori rekli da sme, overlay + accessibility da posmatra druge app-ove = tehnika stalkerware-a i honeypot. Legitimna upotreba overlay-a (ako uopšte): diskretan plutajući toggle za brz ulaz/izlaz — UX, ne špijuniranje.

---

## 6. Lažna GPS lokacija (Leči)

Cilj: tačan grad/kvart, lažna tačna tačka (ulica/zgrada). Ime: **coarsening / obfuskacija lokacije.**

**Preporučen pristup za demo i produkciju (sigurniji):** korisnica deli sa poverljivim kontaktom na nivou kvarta po sopstvenom izboru. Nema detekcije, nema eskalacije.

**Mock GPS protiv nasilnikovog trackera (rizično):** moguće na Androidu (mock provider), ALI tracker može da pročita `isFromMockProvider()` → odaje da se aktivno krije → eskalacija. Ako se koristi, **mora se istaći taj rizik u pitchu.**

**Tehnika (demo):** uzmi pravi `getCurrentPosition`, dodaj **slučajan pomak unutar radijusa** (ne puko zaokruživanje — mreža se lako reverse-engineer-uje).

Cheat-sheet (decimale lat/lng):
- 3 decimale ≈ 111 m (blok)
- 2 decimale ≈ 1.1 km (kvart/grad) ← „grad tačan, tačka lažna"
- 1 decimala ≈ 11 km

Demo: dva pina na mapi — „tačno" vs „podeljeno (nivo kvarta)".

---

## 7. Bezbedan redosled (Seči)

Impulsivno Seči je opasno — zato gate iza „jesi na bezbednom mestu? imaš kuda ako eskalira?".

1. **Dokumentuj sve** (evidence vault) pre ijedne izmene.
2. **Promeni lozinke sa uređaja koji on NE kontroliše**, upali 2FA na telefon koji **on ne drži**.
3. **Izbaci njegove sesije**, skini ga kao recovery, napusti Family Sharing.
4. **Ukloni stalkerware**, ugasi/predaj tracker.
5. **Tajming uskladi sa ASTRA-om** / safety planom.

### AirTag — kako se skida
1. **Nađi:** Android „Unknown tracker alerts" + ručni skener; Apple „Tracker Detect" (i za Android); open-source **AirGuard** (TU Darmstadt) — *referenciraj*, ne reimplementiraj BLE protokol za 48h.
2. **Ugasi:** pritisni poliranu metalnu poleđinu i okreni suprotno od kazaljke → poklopac iskoči → izvadi CR2032.
3. **DV caveat:** nagli nestanak sa mreže može da ga alarmira. Serijski broj je vezan za **njegov** Apple ID = dokaz za policiju. Dokumentuj → odluči da li skidanje odaje → po mogućstvu preda policiji.

---

## 8. Tech stack i platforma

- **Native Android (Kotlin)** — nije samo pitch story, **nužno je**: web/PWA ne može da čita instalirane app-ove ni dozvole. Maska je i bolja na native nego na PWA (PWA na home screen-u se prepozna kao web app).
- **Maska:** `activity-alias` (bezazlen label + ikona), tajni kod u numeričko polje otvara Skadi, `FLAG_SECURE` + exclude-from-recents na Skadi ekranima.
- **Skener bez root-a:** `PackageManager` za listu paketa + tražene dozvole; `Settings.Secure` za Accessibility/Notification listeners; `DevicePolicyManager` za device admin; IOC lista bundlovana kao JSON.
- **Testiranje:** **pravi telefon preko USB-a (adb)**, ne emulator. Emulator nema realne app-ove, pravi Bluetooth (AirTag test), ni tvoje sesije. (Ako baš treba AVD: ostavi ~30–40 GB; full VM: 30–60 GB virtuelnog diska. Na 2TB NVMe nije problem. i7-8700K ima VT-x; na Win10 koristi Windows Hypervisor Platform, HAXM je deprecated.)

---

## 9. MVP scope za 48h (rok: sutra 12h = mentorska sesija, bira top 10)

Do podneva treba **jedan vertikalni slajs koji ispriča celu priču za 3 min**, ne ceo proizvod.

**Gradi do podneva:**
1. **Decoy „Voda"** — radi kao tracker (ml, cilj, streak). Izdrži 10s tuđeg skrola.
2. **Tajni kod** u ml polje → otvara Skadi.
3. **Jedan stvaran skan** koji upali sideloadovanu lažnu „System Update" (dozvole + IOC match). To je wow-moment, mora da bude pravi.
4. **Leči/Seči toggle:** Leči = mapa sa dva pina (tačno vs nivo kvarta). Seči = checklist bezbednog redosleda + generator lozinki + deep-link na Google Password Checkup. Seči gate-ovan iza „jesi na bezbednom mestu".

**Pičuj kao roadmap (NE gradi noćas):** sesije-audit, AirGuard/BLE skener, evidence vault export (PDF), unifikacija sa Šaptom.

> Ako Kotlin od nule preti roku: skener pokaži kao „radi u toku", ali spine (decoy → kod → Leči/Seči) **mora** da klikće.

### Demo flow (5 min)
„Voda" → ukucaš kod → Skadi → skan upali lažnu „System Update" + aktivni Google location-share → biraš **Leči** (harm reduction, ništa vidljivo) ili **Seči** (exit, bezbedan redosled, gate-ovan).

---

## 10. Skaliranje (K5)

Skadi (tema 3) i Šapat (tema 1 — tihi alarm) dele **isti decoy shell + isti hidden trigger + isti evidence vault**. Prirodno je da budu **jedna platforma**: decoy → kod → izbor: tihi alarm (Šapat) ili tracking-check (Skadi). To je „continuation" story u pitchu. **Za 48h gradi samo Skadi do kraja; unifikaciju samo pičuj.**

---

## 11. Srpski resursi (ugraditi u app)

- ASTRA SOS: 011/785-0000
- Autonomni ženski centar: 0800 100 007
- Prijava nasilja: 0800 100 600
- Policija: 192

---

## 12. GitHub description

**One-liner (repo description):**
> Survivor-centered anti-tracking tool disguised as a water tracker — finds how an abuser may be monitoring you, then helps cut it off safely.

**README blurb:**
> **Skadi** is a discreet anti-tracking tool for survivors of tech-facilitated abuse, built for the SheSafe Hackathon (June 2026). It hides behind a fully functional water-tracking app; a secret code opens the safety layer, so an abuser with physical access to the phone sees only a lifestyle app.
>
> Skadi audits the real ways abusers track partners — over-permissioned and known stalkerware apps, abused account sessions, and unwanted location sharing — and treats every finding as a safety event, never a one-tap "delete."
>
> Two modes reflect a survivor's actual situation:
> - **Leči (Heal)** — harm reduction while still in the relationship: coarsens shared GPS to neighborhood level and changes nothing the abuser can detect, buying time.
> - **Seči (Cut)** — for the exit moment: a safe-sequenced cleanup (document first, then re-secure accounts, revoke sessions, remove trackers) gated behind a safety check.
>
> Detection is the easy part — Skadi is built around doing it *without* escalating danger.
