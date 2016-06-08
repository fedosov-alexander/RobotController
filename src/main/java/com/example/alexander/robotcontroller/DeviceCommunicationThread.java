package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by Alexander on 23.05.2016.
 */
public class DeviceCommunicationThread extends Thread {
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private boolean mIsRunning;
    private Byte mMessage = null;
    private Handler mHandler;
    private int UpdatedInstance = 0;

    private DeviceCommunicationThread(BluetoothSocket sock, Handler handler) {
        mBluetoothSocket = sock;
        mHandler = handler;
    }

    public static DeviceCommunicationThread init(BluetoothSocket sock, Handler handler) throws Exception {
        DeviceCommunicationThread thread = null;
        try {
            thread = new DeviceCommunicationThread(sock, handler);
            thread.setInputStream(thread.getBluetoothSocket().getInputStream());
            thread.setOutputStream(thread.getBluetoothSocket().getOutputStream());
            thread.setRunning(true);
            thread.start();
        } catch (Exception e) {

        }
        return thread;
    }

    @Override
    public void run() {

        while (mIsRunning) {
            try {
                if (mInputStream.available() == 4) {
                    byte[] b = new byte[4];//allocating memory to store float
                    mInputStream.read(b);
                    Float f = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    Log.d("DCThread", "instance = " + UpdatedInstance);
                    if (UpdatedInstance != 0) {
                        Message msg = mHandler.obtainMessage(UpdatedInstance, f);
                        msg.sendToTarget();
                        Log.d("DCThread", "send msg to instance = " + UpdatedInstance);
                        UpdatedInstance = 0;
                    }
                }
                if (mInputStream.available() > 4) {
                    mInputStream.skip(mInputStream.available());
                }
            } catch (Exception e) {

            }
            if (mMessage != null) {
                byte[] arr = new byte[1];
                arr[0] = mMessage.byteValue();
                if (arr[0] == (byte) 11) {
                    UpdatedInstance = 1;
                }
                if (arr[0] == (byte) 12) {
                    UpdatedInstance = 2;
                }
                arr[0] = (byte) 1;
                try {
                    mOutputStream.write(arr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMessage = null;
            }
        }
    }

    public void write(byte b) throws Exception {
        mMessage = new Byte(b);
    }

    public void setRunning(boolean running) {
        mIsRunning = running;
    }

    private BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }

    private void setInputStream(InputStream is) {
        mInputStream = is;
    }

    private void setOutputStream(OutputStream os) {
        mOutputStream = os;
    }
}
