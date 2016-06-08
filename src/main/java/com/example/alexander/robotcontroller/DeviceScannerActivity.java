package com.example.alexander.robotcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Alexander on 22.05.2016.
 */
public class DeviceScannerActivity extends Activity {
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDiscoveredDevices;
    private Set<BluetoothDevice> mSelectedDevices;
    private BluetoothDeviceArrayAdapter mDeviceArrayAdapter;
    private BroadcastReceiver mReceiver;
    private Button mStartSearchingButton;
    private Button mStopSearchingButton;
    private Button mStartActivityButton;
    private ListView mDevicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scanner);
        mStartSearchingButton = (Button)findViewById(R.id.startSearchingButton);
        mStopSearchingButton = (Button)findViewById(R.id.stopSearchingButton);
        mStartActivityButton = (Button)findViewById(R.id.startActivityButton);

        mStartSearchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSearching();
            }
        });
        mStopSearchingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSearching();
            }
        });
        mStartActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((mSelectedDevices.size()<=2)&&(mSelectedDevices.size()>0)){
                    startConnectionManagingActivity();
                }

            }
        });
        mStartSearchingButton.setVisibility(View.INVISIBLE);
        mStopSearchingButton.setVisibility(View.INVISIBLE);
        mStartActivityButton.setVisibility(View.INVISIBLE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDiscoveredDevices = new ArrayList<>();
        mSelectedDevices = new HashSet<BluetoothDevice>();
        mDeviceArrayAdapter =  new BluetoothDeviceArrayAdapter(this, mDiscoveredDevices);
        mDevicesListView = (ListView) findViewById(R.id.devicesList);
        mDevicesListView.setAdapter(mDeviceArrayAdapter);
        mDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedDevices.add(mDiscoveredDevices.get(position));
            }
        });
        if(mBluetoothAdapter == null){
            //TODO: show message and shut down
        } else {
            mReceiver = new BluetoothBroadcastReceiver(this);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mStartSearchingButton.setVisibility(View.VISIBLE);
            mStopSearchingButton.setVisibility(View.VISIBLE);
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }
    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mStartSearchingButton.setVisibility(View.VISIBLE);
                mStopSearchingButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public List<BluetoothDevice> getDiscoveredDevices(){
        return mDiscoveredDevices;
    }
    private void startSearching(){
        mBluetoothAdapter.startDiscovery();
        mStartActivityButton.setVisibility(View.INVISIBLE);
    }
    private void stopSearching(){
        mBluetoothAdapter.cancelDiscovery();
        mStartActivityButton.setVisibility(View.VISIBLE);
    }
    private void startConnectionManagingActivity(){
        Intent intent = new Intent(DeviceScannerActivity.this, ConnectionManagingActivity.class);
        intent.putParcelableArrayListExtra("SelectedDevices", new ArrayList<BluetoothDevice>(mSelectedDevices));
        startActivity(intent);
    }
    public BluetoothDeviceArrayAdapter getAdapter(){
        return mDeviceArrayAdapter;
    }

}
