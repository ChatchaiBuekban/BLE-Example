# BLE-Example

## Create Service

1 Add Service to Project and enable it in AndroidManifest.xml file (if you add service manual)

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
   
    
    
    
