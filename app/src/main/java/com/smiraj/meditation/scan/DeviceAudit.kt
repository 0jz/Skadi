package com.smiraj.meditation.scan

/**
 * Builds the [DeviceSection] check-item list for the Leči report.
 *
 * Two sources:
 *  1. Real data from [SpecialAccessChecker.Result] — accessibility services,
 *     notification listeners, and device admin apps become High/Medium findings.
 *  2. Static guided items — biometrics/PIN, Bluetooth, SIM, browser sync,
 *     USB debugging, unknown-sources. Always shown; user reviews manually.
 *
 * Invariant: nothing is written to disk, Room, or DataStore.
 * Cleared on exitToCover() via LeciReport.demo().
 */
object DeviceAudit {

    /**
     * Build the full device checklist from the special-access scan result.
     * Real findings come first (sorted High → Medium), then static guided items.
     */
    fun buildCheckItems(specialAccess: SpecialAccessChecker.Result): List<DeviceCheckItem> {
        val items = mutableListOf<DeviceCheckItem>()

        // ---- Real findings from SpecialAccessChecker ----------------------

        specialAccess.a11yServices.forEach { app ->
            items += DeviceCheckItem(
                label = "Pristupačnost: ${app.appName}",
                guidance = "Podešavanja → Pristupačnost → Preuzete aplikacije → isključi ili deinstaluj " +
                    "\"${app.appName}\" ako je ne prepoznaješ. Ova aplikacija može čitati sve što pišeš.",
                severity = FindingSeverity.High,
            )
        }

        specialAccess.notificationListeners.forEach { app ->
            items += DeviceCheckItem(
                label = "Pristup obaveštenjima: ${app.appName}",
                guidance = "Podešavanja → Aplikacije → Poseban pristup → Pristup obaveštenjima → " +
                    "isključi \"${app.appName}\" ako je ne prepoznaješ.",
                severity = FindingSeverity.Medium,
            )
        }

        specialAccess.deviceAdmins.forEach { app ->
            items += DeviceCheckItem(
                label = "Admin uređaja: ${app.appName}",
                guidance = "Podešavanja → Bezbednost → Admin aplikacije uređaja → " +
                    "deaktiviraj \"${app.appName}\" ako je ne prepoznaješ.",
                severity = FindingSeverity.Medium,
            )
        }

        // ---- Static guided items ------------------------------------------

        items += DeviceCheckItem(
            label = "Zaključavanje ekrana i biometrika",
            guidance = "Podešavanja → Bezbednost → Zaključavanje ekrana → postavi jak PIN ili lozinku (ne obrazac). " +
                "Ukloni lice ili otisak prsta koji ne prepoznaješ: Podešavanja → Bezbednost → Biometrika.",
            severity = FindingSeverity.High,
        )

        items += DeviceCheckItem(
            label = "Bluetooth — upareni uređaji",
            guidance = "Podešavanja → Bluetooth → Upareni uređaji → ukloni sve uređaje koje ne prepoznaješ. " +
                "Upareni uređaj može pratiti tvoju lokaciju i presretati audio u blizini.",
            severity = FindingSeverity.Medium,
        )

        items += DeviceCheckItem(
            label = "SIM kartica i operaterski nalog",
            guidance = "Kontaktiraj operatera i proveri: ko je vlasnik ugovora, da li postoji " +
                "SIM lock ili Family plan koji dozvoljava uvid u pozive. " +
                "Zatraži prenos vlasništva ili novu SIM karticu ako je potrebno.",
            severity = FindingSeverity.Medium,
        )

        items += DeviceCheckItem(
            label = "Sinhronizacija pregledača (Chrome / Firefox)",
            guidance = "Chrome: Podešavanja → tvoje ime → Sinhronizacija → isključi ili " +
                "promeni Google nalog. Firefox: Podešavanja → Firefox nalog → Odjavite se. " +
                "Istorija pretraživanja i lozinke se sinhronizuju sa svim prijavljenim uređajima.",
            severity = FindingSeverity.Medium,
        )

        items += DeviceCheckItem(
            label = "Instalacija iz nepoznatih izvora",
            guidance = "Podešavanja → Aplikacije → Poseban pristup → Instalacija nepoznatih aplikacija → " +
                "isključi za sve aplikacije koje ne prepoznaješ (npr. preuzimači fajlova, menadžeri datoteka).",
            severity = FindingSeverity.Low,
        )

        items += DeviceCheckItem(
            label = "Opcije za programere (USB otklanjanje grešaka)",
            guidance = "Podešavanja → O telefonu → tapni 7 puta na broj verzije da otvoriš meni. " +
                "Opcije za programere → isključi USB otklanjanje grešaka ako ga ne koristiš. " +
                "Aktivan USB debug dozvoljava pristup svim podacima putem računara.",
            severity = FindingSeverity.Low,
        )

        items += DeviceCheckItem(
            label = "Podaci o korišćenju aplikacija",
            guidance = "Podešavanja → Aplikacije → Poseban pristup → Pristup podacima o korišćenju → " +
                "proveri koje aplikacije mogu videti koje druge aplikacije koristiš i koliko dugo.",
            severity = FindingSeverity.Low,
        )

        return items
    }
}
