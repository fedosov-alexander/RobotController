package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Created by Alexander on 23.05.2016.
 */
public class Robot extends ArduinoDevice {
    private BluetoothSocket mSocket;
    private BluetoothDevice mBluetoothDevice;
    private OutputStream mOutputStream;

    public Robot(BluetoothDevice d) {
        super(d);
        mBluetoothDevice = d;
    }

    public void connect(UUID appUUID) {
        try {
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(appUUID);
            mSocket.connect();
            if (mSocket.isConnected()) {
                mOutputStream = mSocket.getOutputStream();
            }
        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void write(float heading, float length) {
        try {
            if (mSocket.isConnected()) {
                Log.d("Robot", "Heading = " + heading + " length = " + length);
                byte output[] = new byte[8];
                byte arr[];
                arr = float2ByteArray(heading);
                for (int i = 0; i < 4; i++) {
                    output[i] = arr[i];
                }
                arr = float2ByteArray(length);
                for (int i = 4; i < 8; i++) {
                    output[i] = arr[i - 4];
                }
                mOutputStream.write(output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] float2ByteArray(float value) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }
}
