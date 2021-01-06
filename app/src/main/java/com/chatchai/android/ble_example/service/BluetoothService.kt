package com.chatchai.android.ble_example.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlin.random.Random

class BluetoothService : Service() {

    private val binder = LocalServiceBinder()

    override fun onBind(intent: Intent): IBinder {
       return  binder
    }

    inner  class  LocalServiceBinder : Binder(){
        fun getService():BluetoothService = this@BluetoothService
    }

    fun getRandomNumber () : Int{
        return  (0..10).random()
    }
}