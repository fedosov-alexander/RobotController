package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * Created by Alexander on 23.05.2016.
 */
public class Device extends ArduinoDevice {
    private BluetoothSocket mSocket;
    private BluetoothDevice mBluetoothDevice;
    private DeviceCommunicationThread mCommunicator;

    public Device(BluetoothDevice d) {
        super(d);
        mBluetoothDevice = d;
    }

    public void connect(UUID appUUID, Handler handler) {
        try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(appUUID);
            mSocket.connect();
        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        try {
            if (mSocket.isConnected()) {
                mCommunicator = DeviceCommunicationThread.init(mSocket, handler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(byte b) throws Exception {
        Log.d("Device", "Write byte = " + b);
        if (mSocket.isConnected())
            if (mCommunicator != null) {
                mCommunicator.write(b);
            }
    }

    public void closeConnection() throws Exception {
        mCommunicator.setRunning(false);
        mSocket.close();
        mCommunicator.join();
    }

    public boolean isConnected() {
        if (mSocket != null) {
            return mSocket.isConnected();
        } else {
            return false;
        }
    }
}
