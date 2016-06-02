package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Alexander on 23.05.2016.
 */
public class DeviceCommunicationThread extends Thread {
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private Handler mMessageHandler;
    private boolean mIsRunning;
    private Byte mMessage = null;
    private DeviceCommunicationThread(BluetoothSocket sock,Handler h){
        mBluetoothSocket = sock;
        mMessageHandler = h;
    }

    public static DeviceCommunicationThread init(BluetoothSocket sock,Handler h)throws Exception{
        DeviceCommunicationThread thread = null;
        try{
            thread = new DeviceCommunicationThread(sock,h);
            thread.setInputStream(thread.getBluetoothSocket().getInputStream());
            thread.setOutputStream(thread.getBluetoothSocket().getOutputStream());
            thread.setRunning(true);
            thread.start();
        } catch(Exception e){

        }
        return thread;
    }
    @Override
    public void run(){

        while (mIsRunning){
            try{
                if(mInputStream.available()>0){
                    byte[] b = new byte[4];//allocating memory to store float
                    mInputStream.read(b);
                    float f = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putFloat("height",f);
                    msg.setData(bundle);
                    mMessageHandler.sendMessage(msg);
                    Log.d("DeviceCommunicationThr","Read float = "+f);
                }
            }catch (Exception e){

            }
            if(mMessage!=null){
                byte[] arr = new byte[1];
                arr[0] = mMessage.byteValue();
                try {
                    mOutputStream.write(arr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMessage = null;
            }
        }
    }
    public void write(byte b) throws  Exception{
        mMessage = new Byte(b);
        Log.d("DeviceCommunicationThr","Writing byte = "+b);
    }
    public void setRunning(boolean running){
        mIsRunning = running;
    }
    private BluetoothSocket getBluetoothSocket(){
        return mBluetoothSocket;
    }
    private void setInputStream(InputStream is){
        mInputStream = is;
    }
    private void setOutputStream(OutputStream os){
        mOutputStream = os;
    }
}
