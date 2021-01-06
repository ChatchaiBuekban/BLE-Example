package com.chatchai.android.ble_example

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import com.chatchai.android.ble_example.databinding.ActivityMainBinding
import com.chatchai.android.ble_example.service.BluetoothService
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    lateinit var mBluetoothService: BluetoothService

    private  val MY_PERMISSION_REQUEST_CODE = 100
    private  val REQUEST_BLUETOOTH_ENABLE = 200
    private  var bluetoothLeScanner: BluetoothLeScanner? = null

    private var device:BluetoothDevice ? = null

    private lateinit var mBinding : ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // binding view
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        //set view
        setContentView(mBinding.root)
        //init view
        with(mBinding){
            connectButton.isEnabled = false
            connectButton.setOnClickListener {
                connectButton.text = "Connecting..."
                mBluetoothService.connect(device)
                Log.d("BLETAG","Connecting... to device : ${device?.name} mac: ${device?.address}")
            }
        }

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

    override fun onDestroy() {
        super.onDestroy()
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
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)
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
        val setting = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        //val scanFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("0000FA00-0000-1000-8000-00805F9B34FB"))).build() //  custom service uuid
        val scanFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"))).build() //   Heart rate service uuid
        val filters = ArrayList<ScanFilter>()
        filters.add(scanFilter)
        bluetoothLeScanner?.startScan(filters,setting,mLeScanCallBack)
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
            device = result?.device
            with(mBinding){
                connectButton.isEnabled = true
            }
            stopScan()
            //mBluetoothService.connect(device)
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
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when(action){
                BluetoothAdapter.ACTION_STATE_CHANGED->{
                    val blueToothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)
                    if (blueToothState==BluetoothAdapter.STATE_ON){
                        startScan() // start scan after bluetooth adapter enabled
                    }
                }
                BluetoothService.ACTION_GATT_CONNECTED -> {
                    with(mBinding){
                        connectButton.text = "Connected"
                    }
                }
                BluetoothService.ACTION_GATT_DISCONNECTED ->{
                    with(mBinding){
                        connectButton.text = "Connect"
                    }
                }
            }
        }
    }
}