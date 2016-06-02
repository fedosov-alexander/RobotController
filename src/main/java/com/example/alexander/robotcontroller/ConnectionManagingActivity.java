package com.example.alexander.robotcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Alexander on 23.05.2016.
 */
public class ConnectionManagingActivity extends Activity {
    private ArrayList<BluetoothDevice> mSelectedDevices;
    private ArrayList<ArduinoDevice> mArduinoDevices;
    private ArduinoDeviceArrayAdapter mArduinoDeviceArrayAdapter;
    private ListView mArduinoDevicesListView;
    private Button mStartActivityButton;
    private BroadcastReceiver mPairingReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_manager);
        mStartActivityButton = (Button)findViewById(R.id.applyButton);
        mStartActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startControllingActivity();
            }
        });
        mArduinoDevices = new ArrayList<ArduinoDevice>();
        mSelectedDevices = getIntent().getParcelableArrayListExtra("SelectedDevices");
        if(mSelectedDevices != null){
            for(BluetoothDevice d : mSelectedDevices){
                mArduinoDevices.add(new ArduinoDevice(d));
            }
        } else {
            //TODO: check for errors!
        }
        mArduinoDeviceArrayAdapter =  new ArduinoDeviceArrayAdapter(this, mArduinoDevices);
        mArduinoDevicesListView = (ListView) findViewById(R.id.arduinoDevices);
        mArduinoDevicesListView.setAdapter(mArduinoDeviceArrayAdapter);
        mArduinoDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArduinoDevice d = mArduinoDevices.get(position);
               if(d.getDerivedType()==null){
                   d.setType(ArduinoDeviceType.ROBOT);
               } else {
                   if(d.getDerivedType()==ArduinoDeviceType.ROBOT){
                       d.setType(ArduinoDeviceType.DEVICE);
                   } else{
                       d.setType(ArduinoDeviceType.ROBOT);
                   }

               }
                TextView tv = (TextView) view.findViewById(R.id.deviceType);
                tv.setText("Type: " +d.getDerivedType());
            }
        });
        mPairingReceiver = new  BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                       // showToast("Paired");
                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                       // showToast("Unpaired");
                    }

                }
            }
        };
    }

    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mPairingReceiver, intent);

    }
    @Override
    protected void onPause(){
        super.onPause();

    }
    private void startControllingActivity(){

        Intent intent = new Intent(ConnectionManagingActivity.this, RobotControllerActivity.class);
        intent.putParcelableArrayListExtra("ArduinoDevices",  mArduinoDevices);
        startActivity(intent);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

    }

}
