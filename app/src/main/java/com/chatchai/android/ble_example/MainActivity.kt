package com.chatchai.android.ble_example

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.chatchai.android.ble_example.service.BluetoothService

class MainActivity : AppCompatActivity() {

    lateinit var mBluetoothService: BluetoothService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this,BluetoothService::class.java)
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
    }

    private val serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //service connected
            Log.d("BLETAG","Service Connected")
            val binder = service as BluetoothService.LocalServiceBinder
            mBluetoothService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            //service disconnected
            Log.d("BLETAG","Service Disconnected")
        }
    }
}