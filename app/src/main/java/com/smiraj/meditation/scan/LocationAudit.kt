package com.smiraj.meditation.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Guided location-exposure findings and a coarsened-location message template.
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
            severity = FindingSeverity.High,
            actionUrl = "https://support.apple.com/en-us/HT210400",
        ),
        LocationFinding(
            label = "Mobilni operater — praćenje poziva",
            description = "Neke porodične tarife uključuju pregled poziva i podataka o lokaciji " +
                "za sve linije na računu. Proverite ko ima vlasnički pristup ugovoru.",
            severity = FindingSeverity.High,
            actionUrl = "https://www.mts.rs/privatni-korisnici/",
        ),
    )

    // Ordered map: OPSTR → human-readable Serbian label.
    // Duplicate labels (FINE + COARSE → "Lokacija") are collapsed in output.
    private val OP_LABELS = linkedMapOf(
        AppOpsManager.OPSTR_FINE_LOCATION          to "Lokacija",
        AppOpsManager.OPSTR_COARSE_LOCATION        to "Lokacija",
        AppOpsManager.OPSTR_RECORD_AUDIO           to "Mikrofon",
        AppOpsManager.OPSTR_CAMERA                 to "Kamera",
        AppOpsManager.OPSTR_READ_CONTACTS          to "Kontak