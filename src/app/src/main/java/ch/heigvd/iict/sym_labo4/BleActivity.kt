package ch.heigvd.iict.sym_labo4

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import ch.heigvd.iict.sym_labo4.abstractactivies.BaseTemplateActivity
import ch.heigvd.iict.sym_labo4.adapters.ResultsAdapter
import ch.heigvd.iict.sym_labo4.viewmodels.BleOperationsViewModel
import java.util.*

/**
 * Project: Labo4
 * Created by fabien.dutoit on 11.05.2019
 * Updated by fabien.dutoit on 06.11.2020
 * (C) 2019 - HEIG-VD, IICT
 *
 * Updated for the assignment by soulaymane.lamrani on 23.01.2021
 */
class BleActivity : BaseTemplateActivity() {
    //system services
    private lateinit var bluetoothAdapter: BluetoothAdapter

    //view model
    private lateinit var bleViewModel: BleOperationsViewModel

    //gui elements
    private lateinit var operationPanel: View
    private lateinit var scanPanel: View
    private lateinit var scanResults: ListView
    private lateinit var emptyScanResults: TextView
    //temperature
    private lateinit var tempTextView: TextView
    private lateinit var tempBtnRead: Button
    //clicked button
    private lateinit var clicksTextView: TextView
    //send integer
    private lateinit var intEditText: EditText
    private lateinit var intBtnSend: Button
    //time
    private lateinit var timeTextView: TextView
    private lateinit var timeBtnSend: Button

    //menu elements
    private var scanMenuBtn: MenuItem? = null
    private var disconnectMenuBtn: MenuItem? = null

    //adapters
    private lateinit var scanResultsAdapter: ResultsAdapter

    //states
    private var handler = Handler(Looper.getMainLooper())

    //characteristics uuids
    private val PARCEL_UUID_CUSTOM_SYM =
            ParcelUuid(UUID.fromString("3c0a1000-281d-4b48-b2a7-f15579a1c38f"))

    private var isScanning = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble)

        //enable and start bluetooth - initialize bluetooth adapter
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        //link GUI
        operationPanel   = findViewById(R.id.ble_operation)
        scanPanel        = findViewById(R.id.ble_scan)
        scanResults      = findViewById(R.id.ble_scanresults)
        emptyScanResults = findViewById(R.id.ble_scanresults_empty)
        //temp
        tempTextView     = findViewById(R.id.text_view_temp)
        tempBtnRead      = findViewById(R.id.button_get_temp)
        //clicks
        clicksTextView   = findViewById(R.id.text_view_clicks_nb)
        //send int
        intEditText      = findViewById(R.id.edit_text_integer)
        intBtnSend       = findViewById(R.id.button_send_integer)
        //time
        timeTextView     = findViewById(R.id.text_view_time)
        timeBtnSend      = findViewById(R.id.button_send_current_time)

        //manage scanned item
        scanResultsAdapter = ResultsAdapter(this)
        scanResults.adapter = scanResultsAdapter
        scanResults.emptyView = emptyScanResults

        //connect to view model
        bleViewModel = ViewModelProvider(this).get(BleOperationsViewModel::class.java)

        updateGui()

        //events
        scanResults.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            runOnUiThread {
                //we stop scanning
                scanLeDevice(false)
                //we connect
                bleViewModel.connect(scanResultsAdapter.getItem(position).device)
            }
        }

        //ble events
        bleViewModel.isConnected.observe(this, { updateGui() })

        //temp events
        tempBtnRead.setOnClickListener { bleViewModel.readTemperature() }
        bleViewModel.temperature.observe(this)
        { temp -> tempTextView.text = String.format(Locale.getDefault(), "%d°C", temp) }

        //clicks events
        bleViewModel.buttonClicks.observe(this) { c -> clicksTextView.text = c.toString()}

        //int events
        intBtnSend.setOnClickListener {
            intEditText.text.toString().let { bleViewModel.sendInt(it.toInt()) }
        }

        //time events
        bleViewModel.currentTime.observe(this) {time -> timeTextView.text = time}
        timeBtnSend.setOnClickListener {bleViewModel.sendTime()}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.ble_menu, menu)
        //we link the two menu items
        scanMenuBtn = menu.findItem(R.id.menu_ble_search)
        disconnectMenuBtn = menu.findItem(R.id.menu_ble_disconnect)
        //we update the gui
        updateGui()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_ble_search) {
            if (isScanning) scanLeDevice(false) else scanLeDevice(true)
            return true
        } else if (id == R.id.menu_ble_disconnect) {
            bleViewModel.disconnect()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        if (isScanning) scanLeDevice(false)
        if (isFinishing) bleViewModel.disconnect()
    }

    /*
     * Method used to update the GUI according to BLE status:
     * - connected: display operation panel (BLE control panel)
     * - not connected: display scan result list
     */
    private fun updateGui() {
        val isConnected = bleViewModel.isConnected.value
        if (isConnected != null && isConnected) {

            scanPanel.visibility = View.GONE
            operationPanel.visibility = View.VISIBLE

            if (scanMenuBtn != null && disconnectMenuBtn != null) {
                scanMenuBtn!!.isVisible = false
                disconnectMenuBtn!!.isVisible = true
            }
        } else {
            operationPanel.visibility = View.GONE
            scanPanel.visibility = View.VISIBLE

            if (scanMenuBtn != null && disconnectMenuBtn != null) {
                disconnectMenuBtn!!.isVisible = false
                scanMenuBtn!!.isVisible = true
            }
        }
    }

    // this method need user granted localisation permission,
    // our demo app is requesting it on MainActivity
    private fun scanLeDevice(enable: Boolean) {
        val bluetoothScanner = bluetoothAdapter.bluetoothLeScanner

        if (enable) {
            //config
            val builderScanSettings = ScanSettings.Builder()
            builderScanSettings.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            builderScanSettings.setReportDelay(0)

            //we scan for any BLE device
            //we don't filter them based on advertised services...
            // ajouter un filtre pour n'afficher que les devices proposant
            // le service "SYM" (UUID: "3c0a1000-281d-4b48-b2a7-f15579a1c38f")

            //reset display
            scanResultsAdapter.clear()

            val filter = ScanFilter.Builder().setServiceUuid(PARCEL_UUID_CUSTOM_SYM).build()

            bluetoothScanner.startScan(Collections.singletonList(filter),
                    builderScanSettings.build(), leScanCallback)

            Log.d(TAG, "Start scanning...")
            isScanning = true

            //we scan only for 15 seconds
            handler.postDelayed({ scanLeDevice(false) }, 15 * 1000L)
        } else {
            bluetoothScanner.stopScan(leScanCallback)
            isScanning = false
            Log.d(TAG, "Stop scanning (manual)")
        }
    }

    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            runOnUiThread { scanResultsAdapter.addDevice(result) }
        }
    }

    companion object {
        private val TAG = BleActivity::class.java.simpleName
    }
}