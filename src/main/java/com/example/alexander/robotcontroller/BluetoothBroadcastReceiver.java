package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Alexander on 22.05.2016.
 */
public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private DeviceScannerActivity mScanner;
    public BluetoothBroadcastReceiver(DeviceScannerActivity scannerActivity){
        mScanner = scannerActivity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            mScanner.getAdapter().add(device);
        }
    }
}
