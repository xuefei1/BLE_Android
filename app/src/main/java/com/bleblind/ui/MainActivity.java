package com.bleblind.ui;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.bleblind.R;
import com.bleblind.components.MyBLEService;
import com.bleblind.components.Profile;
import com.bleblind.support.SeekArc;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private BluetoothGattCharacteristic characteristicTx = null;
    private MyBLEService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;

    SeekArc seekArc;
    ProgressDialog dialog;

    private String targetAddress = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((MyBLEService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(targetAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekArc = (SeekArc) findViewById(R.id.seekArc);
        initHalfSeekArc();
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                byte buf[] = new byte[] { Profile.CMD_TYPE_SET, Profile.CMD_SET_LEVEL, (byte) (seekArc.getProgress()+1), 0x0, 0x0 };
                characteristicTx.setValue(buf);
                mBluetoothLeService.writeCharacteristic(characteristicTx);
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setTitle("Connecting");
        dialog.show();

        targetAddress = getIntent().getExtras().getString(ScanActivity.TARGET_DEVICE_ADDRESS_KEY);
        if(targetAddress == null || targetAddress.isEmpty()) finish();

        disableUIElements();

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        Intent gattServiceIntent = new Intent(getApplicationContext(),
                MyBLEService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initHalfSeekArc() {
        seekArc.setSweepAngle(180);
        seekArc.setStartAngle(0);
        seekArc.setTouchInSide(true);
        seekArc.setRoundedEdges(true);
        seekArc.setArcRotation(-90);
        seekArc.setMax(Profile.SUPPORTED_POSITIONS-1);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(MyBLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(MyBLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(MyBLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(MyBLEService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(MyBLEService.ACTION_GATT_RSSI);

        return intentFilter;
    }

    @Override
    protected void onStop() {
        super.onStop();
        disableUIElements();
        unbindService(mServiceConnection);
        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            finish();
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (MyBLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Disconnected",
                        Toast.LENGTH_SHORT).show();
                finish();
            } else if (MyBLEService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                Toast.makeText(getApplicationContext(), "Connected",
                        Toast.LENGTH_SHORT).show();
                enableUIElements();
                getGattService(mBluetoothLeService.getSupportedGattService());
            } else if (MyBLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                Toast.makeText(getApplicationContext(), "Data available",
                        Toast.LENGTH_SHORT).show();

            } else if (MyBLEService.ACTION_GATT_RSSI.equals(action)) {

            }
        }
    };

    private void getGattService(BluetoothGattService gattService) {
        if (gattService == null)
            return;

        characteristicTx = gattService
                .getCharacteristic(MyBLEService.UUID_BLE_SHIELD_TX);

        BluetoothGattCharacteristic characteristicRx = gattService
                .getCharacteristic(MyBLEService.UUID_BLE_SHIELD_RX);
        mBluetoothLeService.setCharacteristicNotification(characteristicRx,
                true);
        mBluetoothLeService.readCharacteristic(characteristicRx);
    }

    private void enableUIElements(){
        dialog.dismiss();
        seekArc.setEnabled(true);
    }

    private void disableUIElements(){
        seekArc.setEnabled(false);
    }
}
