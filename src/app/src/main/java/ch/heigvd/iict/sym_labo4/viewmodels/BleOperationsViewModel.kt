package ch.heigvd.iict.sym_labo4.viewmodels

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.observer.ConnectionObserver
import java.nio.ByteBuffer
import java.util.*


/**
 * Project: Labo4
 * Created by fabien.dutoit on 11.05.2019
 * Updated by fabien.dutoit on 06.11.2020
 * (C) 2019 - HEIG-VD, IICT
 *
 * Updated for the assignment by soulaymane.lamrani on 23.01.2021
 */
class BleOperationsViewModel(application: Application) : AndroidViewModel(application) {

    private var ble = SYMBleManager(application.applicationContext)
    private var mConnection: BluetoothGatt? = null

    //live data - observer
    val isConnected  = MutableLiveData(false)
    val temperature  = MutableLiveData<Int>()
    val buttonClicks = MutableLiveData<Int>()
    val currentTime  = MutableLiveData<String>()

    //Services and Characteristics of the SYM Pixl
    private var timeService: BluetoothGattService?            = null
    private var symService: BluetoothGattService?             = null
    private var currentTimeChar: BluetoothGattCharacteristic? = null
    private var integerChar: BluetoothGattCharacteristic?     = null
    private var temperatureChar: BluetoothGattCharacteristic? = null
    private var buttonClickChar: BluetoothGattCharacteristic? = null

    // Services and Charateristics UUIDs
    private val SYM_CUS_UUID = UUID.fromString("3c0a1000-281d-4b48-b2a7-f15579a1c38f")
    private val TIME_S_UUID  = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
    private val INT_C_UUID   = UUID.fromString("3c0a1001-281d-4b48-b2a7-f15579a1c38f")
    private val TEMP_C_UUID  = UUID.fromString("3c0a1002-281d-4b48-b2a7-f15579a1c38f")
    private val CTIME_C_UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
    private val BTN_C_UUID   = UUID.fromString("3c0a1003-281d-4b48-b2a7-f15579a1c38f")

    //Current Time indexes
    private val HOUR_ID    = 4
    private val MIN_ID     = 5
    private val SEC_ID     = 6

    //Current Time size
    private val C_S_SIZE   = 10

    //Current time adjustment
    private val MONTH_ADJ  = 1
    private val D_OF_W_ADJ = 2

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared")
        ble.disconnect()
    }

    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "User request connection to: $device")
        if (!isConnected.value!!) {
            ble.connect(device)
                    .retry(1, 100)
                    .useAutoConnect(false)
                    .enqueue()
        }
    }

    fun disconnect() {
        Log.d(TAG, "User request disconnection")
        ble.disconnect()
        mConnection?.disconnect()
    }

    /*
        vous pouvez placer ici les différentes méthodes permettant à l'utilisateur
        d'interagir avec le périphérique depuis l'activité
     */

    fun readTemperature(): Boolean {
        return if (!isConnected.value!! || temperatureChar == null)
            false
        else
            ble.readTemperature()
    }

    fun sendInt(value: Int): Boolean {
        return if (!isConnected.value!! || integerChar == null)
            false
        else
            ble.sendInt(value)
    }

    fun sendTime(): Boolean {
        return if (!isConnected.value!! || currentTimeChar == null)
            false
        else
            ble.sendTime()
    }

    private val bleConnectionObserver: ConnectionObserver = object : ConnectionObserver {
        override fun onDeviceConnecting(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceConnecting")
            isConnected.value = false
        }

        override fun onDeviceConnected(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceConnected")
            isConnected.value = true
        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceDisconnecting")
            isConnected.value = false
        }

        override fun onDeviceReady(device: BluetoothDevice) {
            Log.d(TAG, "onDeviceReady")
        }

        override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
            Log.d(TAG, "onDeviceFailedToConnect")
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
            if(reason == ConnectionObserver.REASON_NOT_SUPPORTED) {
                Log.d(TAG, "onDeviceDisconnected - not supported")
                Toast.makeText(getApplication()
                        , "Device not supported - implement method isRequiredServiceSupported()"
                        , Toast.LENGTH_LONG).show()
            }
            else
                Log.d(TAG, "onDeviceDisconnected")
            isConnected.value = false
        }

    }

    private inner class SYMBleManager(applicationContext: Context) : BleManager(applicationContext) {
        /**
         * BluetoothGatt callbacks object.
         */
        private var mGattCallback: BleManagerGattCallback? = null

        public override fun getGattCallback(): BleManagerGattCallback {
            //we initiate the mGattCallback on first call, singleton
            if (mGattCallback == null) {
                mGattCallback = object : BleManagerGattCallback() {

                    public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
                        mConnection = gatt //trick to force disconnection

                        Log.d(TAG, "isRequiredServiceSupported - TODO")

                        /*
                        - Nous devons vérifier ici que le périphérique auquel on vient de se
                          connecter possède bien tous les services et les caractéristiques
                          attendues, on vérifiera aussi que les caractéristiques présentent bien les
                          opérations attendues
                        - On en profitera aussi pour garder les références vers les différents
                          services et caractéristiques (déclarés en lignes 39 à 44)
                        */

                        symService      = mConnection!!.getService(SYM_CUS_UUID)
                        temperatureChar = symService!!.getCharacteristic(TEMP_C_UUID)
                        integerChar     = symService!!.getCharacteristic(INT_C_UUID)
                        buttonClickChar = symService!!.getCharacteristic(BTN_C_UUID)

                        timeService     = mConnection!!.getService(TIME_S_UUID)
                        currentTimeChar = timeService!!.getCharacteristic(CTIME_C_UUID)

                        return if (temperatureChar != null && integerChar     != null &&
                                   buttonClickChar != null && currentTimeChar != null)
                            true
                        else {
                            bleConnectionObserver.onDeviceDisconnected(bluetoothDevice,
                                    ConnectionObserver.REASON_NOT_SUPPORTED)
                            false //si tout est OK, on retourne true, sinon la librairie appelera la
                                  //méthode onDeviceDisconnected() avec le flag REASON_NOT_SUPPORTED
                        }
                    }

                    override fun initialize() {
                        /*
                            Ici nous somme sûr que le périphérique possède bien tous les services
                            et caractéristiques attendus et que nous y sommes connectés. Nous
                            pouvous effectuer les premiers échanges BLE: Dans notre cas il s'agit de
                            s'enregistrer pour recevoir les notifications proposées par certaines
                            caractéristiques, on en profitera aussi pour mettre en place les
                            callbacks correspondants.
                         */
                        setNotificationCallback(buttonClickChar).with { _, data ->
                            buttonClicks.value = data.getIntValue(Data.FORMAT_UINT8, 0)
                        }
                        setNotificationCallback(currentTimeChar).with { _, data ->
                            currentTime.value = String.format("%02d:%02d:%02d",
                                    data.getIntValue(Data.FORMAT_UINT8, HOUR_ID)!!.toInt(),
                                    data.getIntValue(Data.FORMAT_UINT8, MIN_ID)!!.toInt(),
                                    data.getIntValue(Data.FORMAT_UINT8, SEC_ID)!!.toInt())
                        }

                        enableNotifications(buttonClickChar).enqueue()
                        enableNotifications(currentTimeChar).enqueue()
                    }

                    override fun onDeviceDisconnected() {
                        //we reset services and characteristics
                        timeService = null
                        currentTimeChar = null
                        symService = null
                        integerChar = null
                        temperatureChar = null
                        buttonClickChar = null
                    }
                }
            }
            return mGattCallback!!
        }

        fun readTemperature(): Boolean {
            /*
                on peut effectuer ici la lecture de la caractéristique température
                la valeur récupérée sera envoyée à l'activité en utilisant le mécanisme
                des MutableLiveData
                On placera des méthodes similaires pour les autres opérations
            */
            readCharacteristic(temperatureChar).with { _, data ->
                temperature
                        .postValue(data.getIntValue(Data.FORMAT_UINT16, 0)!! / 10)
            }.enqueue()
            return mConnection!!.readCharacteristic(temperatureChar)
        }

        fun sendInt(value: Int): Boolean {
            writeCharacteristic(integerChar,
                    ByteBuffer.allocate(4).putInt(value).array()).enqueue()
            return mConnection!!.writeCharacteristic(integerChar)
        }

        fun sendTime(): Boolean {
            val cal = Calendar.getInstance()

            writeCharacteristic(currentTimeChar, ByteBuffer.allocate(C_S_SIZE)
                    .put((cal.get(Calendar.YEAR) and 0xFF).toByte())
                    .put(((cal.get(Calendar.YEAR) shr 8) and 0xFF).toByte())
                    .put(((cal.get(Calendar.MONTH) + MONTH_ADJ) % 12).toByte())
                    .put(cal.get(Calendar.DAY_OF_MONTH).toByte())
                    .put(cal.get(Calendar.HOUR_OF_DAY).toByte())
                    .put(cal.get(Calendar.MINUTE).toByte())
                    .put(cal.get(Calendar.SECOND).toByte())
                    .put(((cal.get(Calendar.DAY_OF_WEEK) + D_OF_W_ADJ) % 7).toByte())
                    .put((cal.get(Calendar.SECOND)/256).toByte())
                    .put(1.toByte())
                    .array()).enqueue()
            return mConnection!!.writeCharacteristic(currentTimeChar)
        }
    }

    companion object {
        private val TAG = BleOperationsViewModel::class.java.simpleName
    }

    init {
        ble.setConnectionObserver(bleConnectionObserver)
    }
}