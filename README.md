# BLE-Example

## Create Service

Create BluetoothService class with Service inheritance to project and enable it in AndroidManifest.xml file (if you add service manual)

- AndroidManifest.xml
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.chatchai.android.ble_example">

    <application
        ...
        <service
            android:name=".service.BluetoothService"
            android:enabled="true"
            android:exported="false" />

        <activity android:name=".MainActivity">
         ....
        </activity>
    </application>

</manifest>
```

- BluetoothService.kt
```
class BluetoothService : Service() {

    private val binder = LocalServiceBinder()

    override fun onBind(intent: Intent): IBinder {
       return  binder
    }

    private inner  class  LocalServiceBinder : Binder(){
        fun getService():BluetoothService = this@BluetoothService
    }
}
```

## Connect service in activity class

- MainActivity.kt
```
class MainActivity : AppCompatActivity() {

    lateinit var service: BluetoothService
   
    ...
   
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
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            //service disconnected
            Log.d("BLETAG","Service Disconnected")
        }
    }
}
```
    

    
