package com.smiraj.meditation.scan

/**
 * Guided location-exposure findings and a coarsened-location message template.
 *
 * All findings are simulated for the demo. They represent the most common ways
 * an abusive partner gains real-time location access via shared accounts and
 * family-sharing features — without any malware required.
 *
 * Invariant: no data is written to disk, Room, or DataStore.
 * Cleared on exitToCover() via LeciReport.demo().
 */
object LocationAudit {

    /**
     * Guided family/account location findings shown in the Leči report.
     * Each finding links to the provider's settings page so the user can
     * review access manually.
     */
    fun demoFindings(): List<LocationFinding> = listOf(
        LocationFinding(
            label = "Google Maps — deljenje lokacije u realnom vremenu",
            description = "Aktivno deljenje lokacije može biti uključeno bez vidljivog obaveštenja. " +
                "Proverite ko ima pristup i na koliko dugo.",
            severity = FindingSeverity.High,
            actionUrl = "https://maps.google.com/sharing",
        ),
        LocationFinding(
            label = "Google Family Link / porodična grupa",
            description = "Porodična grupa na Google nalogu može videti lokaciju u realnom vremenu " +
                "ako je Family Link ili zajednički Google Calendar aktivan.",
            severity = FindingSeverity.High,
            actionUrl = "https://families.google.com/families",
        ),
        LocationFinding(
            label = "Find My Device (Google)",
            description = "Svako ko ima pristup Google nalogu može videti poziciju uređaja putem " +
                "Find My Device. Proverite koje sesije su aktivne na nalogu.",
            severity = FindingSeverity.High,
            actionUrl = "https://myaccount.google.com/find-your-phone",
        ),
        LocationFinding(
            label = "Apple Find My / Porodično deljenje",
            description = "Ako koristite Apple uređaj i Porodično deljenje, ostali članovi grupe " +
                "mogu videti vašu lokaciju u aplikaciji Find My.",
            severity = FindingSeverity.Medium,
            actionUrl = "https://support.apple.com/en-us/HT210400",
        ),
        LocationFinding(
            label = "Mobilni operater — praćenje poziva",
            description = "Neke porodične tarife uključuju pregled poziva i podataka o lokaciji " +
                "za sve linije na računu. Proverite ko ima vlasnički pristup ugovoru.",
            severity = FindingSeverity.Medium,
            actionUrl = "https://www.mts.rs/privatni-korisnici/",
        ),
    )

    /**
     * A coarsened-location message the user can send to a trusted person.
     * Gives approximate whereabouts without revealing the exact address,
     * helping the user maintain safety check-ins without exposing GPS position.
     *
     * This is a template — the user should edit it before sending.
     */
    fun coarsenedMessage(): String =
        "Trenutno sam u centru grada i u redu sam. Javim ti se večeras do 20h."
}
