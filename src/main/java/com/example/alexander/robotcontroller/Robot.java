package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Alexander on 23.05.2016.
 */
public class Robot extends ArduinoDevice {
    private BluetoothSocket mSocket;
    private RobotCommunicator mCommunicator;

        public Robot(BluetoothDevice d){
        super(d);
    }

    public void connect(UUID appUUID) throws Exception{
            mSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(appUUID);
            mCommunicator = new RobotCommunicator();
    }
    public void write(){

    }
}
