package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * Created by Alexander on 23.05.2016.
 */
public class Device extends ArduinoDevice{
    private BluetoothSocket mSocket;
    private BluetoothDevice mBluetoothDevice;
    private DeviceCommunicationThread mCommunicator;
    private Handler mMessageHandler;
    public Device(BluetoothDevice d, Handler h){
        super(d);
        mBluetoothDevice = d;
        mMessageHandler = h;
    }

    public void connect(UUID appUUID) {
        try {
            mSocket = createRfcommSocket(mBluetoothDevice);
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
            mCommunicator = DeviceCommunicationThread.init(mSocket,mMessageHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void write(byte b)throws Exception{
        Log.d("Device","Write called");
        if(mSocket.isConnected())
        if(mCommunicator!=null){
            mCommunicator.write(b);
        }
    }
    public void closeConnection() throws Exception{

        mCommunicator.setRunning(false);
        mSocket.close();
        mCommunicator.join();
    }
    public boolean isConnected(){
        if(mSocket != null){
            return mSocket.isConnected();
        } else {
            return false;
        }
    }
    public static BluetoothSocket createRfcommSocket(BluetoothDevice device) {
        BluetoothSocket tmp = null;
        try {
            Class class1 = device.getClass();
            Class aclass[] = new Class[1];
            aclass[0] = Integer.TYPE;
            Method method = class1.getMethod("createRfcommSocket", aclass);
            Object aobj[] = new Object[1];
            aobj[0] = Integer.valueOf(1);

            tmp = (BluetoothSocket) method.invoke(device, aobj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return tmp;
    }
}
