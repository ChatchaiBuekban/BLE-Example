package com.chatchai.android.ble_example

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.chatchai.android.ble_example.service.BluetoothService

class MainActivity : AppCompatActivity() {

    lateinit var mBluetoothService: BluetoothService

    private  val MY_PERMISSION_REQUEST_CODE = 100
    private  val REQUEST_BLUETOOTH_ENABLE = 200
    private  var bluetoothLeScanner: BluetoothLeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initBluetoothAdapter()
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            //request location access
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),MY_PERMISSION_REQUEST_CODE)
        }
    }

    private fun initBluetoothAdapter() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter==null){
            //device not support bluetooth
            this.finish()
        }else{
           if (!bluetoothAdapter.isEnabled){
               //request enable bluetooth
                val enableBtIntend = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                setResult(Activity.RESULT_OK,enableBtIntend)
                startActivityForResult(enableBtIntend,REQUEST_BLUETOOTH_ENABLE)
            }else{
                if (BluetoothAdapter.getDefaultAdapter().isEnabled){
                    startScan()
                }
           }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==REQUEST_BLUETOOTH_ENABLE){
            if (resultCode!=Activity.RESULT_OK){
                this.finish()
            }
        }
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

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(receiver,intentFilter)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==MY_PERMISSION_REQUEST_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                startScan()
            }else{
                //you denied permission
                this.finish()
            }
        }
    }

    private var isScanning = false
    private fun startScan() {
        Log.d("BLETAG","Start Scanning...")
        bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
        bluetoothLeScanner?.startScan(mLeScanCallBack)
        isScanning = true
    }

    private fun stopScan(){
        Log.d("BLETAG","Stop Scan")
        isScanning = false
        bluetoothLeScanner?.stopScan(mLeScanCallBack)
    }

    private val mLeScanCallBack:ScanCallback = object : ScanCallback(){
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("BLETAG", "Found device name: ${result?.device?.name} mac: ${result?.device?.address}")
        }
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

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED){
                val blueToothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)
                if (blueToothState==BluetoothAdapter.STATE_ON){
                    startScan() // start scan after bluetooth adapter enabled
                }
            }
        }
    }
}