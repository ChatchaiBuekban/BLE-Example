package com.chatchai.android.ble_example.service

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*
import kotlin.random.Random

class BluetoothService : Service() {

    private val binder = LocalServiceBinder()
    var bluetoothGatt : BluetoothGatt? = null

    var connectionState = STATE_DISCONNECTED

    companion object{
        val HEART_RATE_SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
        val HEART_RATE_MEASUREMENT_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
        const val ACTION_GATT_CONNECTED = "com.chatchai.ble_example.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.chatchai.ble_example.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.chatchai.ble_example.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.chatchai.ble_example.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.chatchai.ble_example.EXTRA_DATA"

        const val STATE_DISCONNECTED = 0
        const val STATE_CONNECTING = 1
        const val STATE_CONNECTED = 2
    }

    override fun onBind(intent: Intent): IBinder {
       return  binder
    }

    inner  class  LocalServiceBinder : Binder(){
        fun getService():BluetoothService = this@BluetoothService
    }

//    fun getRandomNumber () : Int{
//        return  (0..10).random()
//    }

    fun connect(device: BluetoothDevice?) {
        bluetoothGatt = device?.connectGatt(this,true,bluetoothGattCallBack)
    }

    private val bluetoothGattCallBack = object : BluetoothGattCallback(){
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when(newState){
                BluetoothProfile.STATE_CONNECTED->{
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    Log.d("BLETAG","Bluetooth gatt device connected: ${gatt?.device?.address}")
                    bluetoothGatt?.discoverServices()
                }
                BluetoothProfile.STATE_CONNECTING ->{
                    connectionState = STATE_CONNECTING
                    Log.d("BLETAG","Bluetooth gatt connecting to : ${gatt?.device?.address}")
                }
                BluetoothProfile.STATE_DISCONNECTED ->{
                    connectionState = STATE_DISCONNECTED
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                    Log.d("BLETAG","Bluetooth gatt device disconnected: ${gatt?.device?.address}")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
                when(status){
                    BluetoothGatt.GATT_SUCCESS -> {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                        Log.d("BLETAG","Gatt Discovered Service")
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

}