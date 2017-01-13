package com.bleblind.ui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bleblind.R;
import com.bleblind.components.MyBLEService;
import com.bleblind.components.Profile;
import com.bleblind.support.ScanListAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Fred on 2017/1/11.
 */
public class ScanActivity extends Activity {

    ListView scanResult;
    Button scan;
    List<BluetoothDevice> listDevices;
    Set<String> discoveredDevices;
    ScanListAdapter adapter;

    public static final String TARGET_DEVICE_ADDRESS_KEY = "Device Address";

    private BluetoothAdapter mBluetoothAdapter;

    private boolean scanFlag = false;

    private static final long SCAN_PERIOD = 1000;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        scanResult = (ListView) findViewById(R.id.lv_scan_result);
        scan = (Button) findViewById(R.id.btn_scan);
        listDevices = new ArrayList<>();
        discoveredDevices = new HashSet<>();
        adapter = new ScanListAdapter(this, R.layout.scan_adapter, listDevices);
        scanResult.setAdapter(adapter);
        scanResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(scanFlag) mBluetoothAdapter.stopLeScan(mLeScanCallback);
                if(listDevices.get(position).getAddress().isEmpty()) return;
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra(TARGET_DEVICE_ADDRESS_KEY, listDevices.get(position).getAddress());
                startActivity(intent);

            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetDeviceList();
                scanLeDevice();
            }
        });

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }
    }

    private void scanLeDevice() {

        new Thread() {

            @Override
            public void run() {
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                scan.setEnabled(false);
                            }
                        }
                );
                scanFlag = true;
                mBluetoothAdapter.startLeScan(mLeScanCallback);

                try {
                    Thread.sleep(SCAN_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                scan.setEnabled(true);
                            }
                        }
                );
                scanFlag = false;
            }
        }.start();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device != null && !discoveredDevices.contains(device.getAddress()) && device.getName() != null && device.getName().contains(Profile.BLE_MODULE_FILTER)) {
                        listDevices.add(device);
                        discoveredDevices.add(device.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        resetDeviceList();
    }

    private void resetDeviceList(){
        listDevices.clear();
        discoveredDevices.clear();
        adapter.notifyDataSetChanged();
    }

}
