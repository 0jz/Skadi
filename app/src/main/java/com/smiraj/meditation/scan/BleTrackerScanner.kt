package com.smiraj.meditation.scan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat

/**
 * Scans for nearby Bluetooth tracking devices that may be used for covert surveillance.
 *
 * Detected device types: Apple AirTag, Tile, Samsung SmartTag.
 *
 * Uses only passive BLE advertisement scanning — no pairing, no connection, no network.
 * Results stay in memory only.
 *
 * Permissions:
 *   Android 12+ (API 31+): BLUETOOTH_SCAN with neverForLocation (no location perm needed).
 *   Android < 12: BLUETOOTH + BLUETOOTH_ADMIN. Scan is silently skipped if Bluetooth is off
 *   or permissions are unavailable.
 */
class BleTrackerScanner(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }

    /** Returns true if BLE scanning is possible right now. */
    fun isAvailable(): Boolean {
        if (bluetoothAdapter?.isEnabled != true) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Starts a 10-second BLE advertisement scan and delivers results via [onDone].
     * [onDone] is called on the main thread. Closest devices (highest RSSI) are first.
     *
     * Call from a background coroutine; the 10-second wait happens on a Handler,
     * so this suspends naturally via suspendCoroutine if needed, or just fire-and-forget.
     */
    fun scan(onDone: (List<BleTrackerFinding>) -> Unit) {
        if (!isAvailable()) { onDone(emptyList()); return }

        val adapter = bluetoothAdapter ?: run { onDone(emptyList()); return }
        val scanner = adapter.bluetoothLeScanner ?: run { onDone(emptyList()); return }

        val found = mutableMapOf<String, BleTrackerFinding>()
        val handler = Handler(Looper.getMainLooper())

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val type = identifyTracker(result) ?: return
                val address = result.device.address ?: return
                // Keep the entry with strongest signal if we see the same address twice
                val existing = found[address]
                if (existing == null || result.rssi > existing.rssi) {
                    val name = runCatching { result.device.name }
                        .getOrNull()
                        ?.takeIf { it.isNotBlank() }
                        ?: type.label
                    found[address] = BleTrackerFinding(
                        deviceName = name,
                        trackerType = type,
                        rssi = result.rssi,
                        address = address,
                    )
                }
            }

            override fun onScanFailed(errorCode: Int) {
                handler.post { onDone(emptyList()) }
            }
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onDone(emptyList()); return
        }

        try {
            scanner.startScan(null, settings, callback)
        } catch (_: Exception) {
            onDone(emptyList()); return
        }

        handler.postDelayed({
            try {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH_SCAN,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    scanner.stopScan(callback)
                }
            } catch (_: Exception) { }
            // Closest trackers first (highest RSSI = strongest signal = nearest)
            onDone(found.values.sortedByDescending { it.rssi })
        }, SCAN_DURATION_MS)
    }

    // ---- Tracker identification -------------------------------------------

    private fun identifyTracker(result: ScanResult): TrackerType? {
        val record = result.scanRecord ?: return null

        // Apple AirTag — manufacturer ID 0x004C
        // Nearby Interaction (0x12 0x19) or Nearby Action (0x44 …)
        val appleData = record.getManufacturerSpecificData(APPLE_MFR_ID)
        if (appleData != null && appleData.size >= 2) {
            if ((appleData[0] == 0x12.toByte() && appleData[1] == 0x19.toByte()) ||
                appleData[0] == 0x44.toByte()
            ) {
                return TrackerType.AirTag
            }
        }

        // Tile — service UUIDs 0xFEED or 0xFEEC
        val serviceUuids = record.serviceUuids
        if (serviceUuids != null) {
            val uuidStrings = serviceUuids.map { it.uuid.toString().uppercase() }
            if (uuidStrings.any { "FEED" in it || "FEEC" in it }) {
                return TrackerType.Tile
            }
        }

        // Samsung SmartTag — manufacturer ID 0x0075
        if (record.getManufacturerSpecificData(SAMSUNG_MFR_ID) != null) {
            return TrackerType.SamsungSmartTag
        }

        return null
    }

    companion object {
        private const val SCAN_DURATION_MS = 10_000L
        private const val APPLE_MFR_ID   = 0x004C
        private const val SAMSUNG_MFR_ID = 0x0075
    }
}

// ---- Result types ---------------------------------------------------------

data class BleTrackerFinding(
    val deviceName: String,
    val trackerType: TrackerType,
    /** Signal strength in dBm — more negative = farther away. */
    val rssi: Int,
    /** BLE MAC address (may be randomized/rotating for privacy). */
    val address: String,
)

enum class TrackerType(val label: String, val warningText: String) {
    AirTag(
        label = "Apple AirTag",
        warningText = "AirTag može biti sakriven u torbi, autu ili odeći. " +
            "Fizički pregledaj svoje stvari.",
    ),
    Tile(
        label = "Tile Tracker",
        warningText = "Tile tracker uočen u blizini. Može biti korišćen za " +
            "praćenje kretanja bez tvog znanja.",
    ),
    SamsungSmartTag(
        label = "Samsung SmartTag",
        warningText = "Samsung tracker u blizini. Proveri da li ga prepoznaješ.",
    ),
}
