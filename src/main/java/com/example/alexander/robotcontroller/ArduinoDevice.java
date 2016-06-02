package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Alexander on 23.05.2016.
 */
public class ArduinoDevice implements Parcelable{
    protected BluetoothDevice mBluetoothDevice;
    private ArduinoDeviceType mDerivedType;

    public ArduinoDevice(BluetoothDevice d){
        mBluetoothDevice = d;
    }
    public ArduinoDevice(Parcel p) {
        mBluetoothDevice = (BluetoothDevice) p.readParcelable(BluetoothDevice.class.getClassLoader());
        String s = p.readString();
        if(s!=null){
            switch (s){
                case "Robot":{
                    mDerivedType = ArduinoDeviceType.ROBOT;
                    break;
                }
                case "Device":{
                    mDerivedType = ArduinoDeviceType.DEVICE;
                    break;
                }
                default: {mDerivedType = null;break;}
            }
        } else {
            mDerivedType = null;
        }
    }
    public String getName(){
        return mBluetoothDevice.getName();
    }
    public String getAddress(){
        return mBluetoothDevice.getAddress();
    }
    public BluetoothDevice getBluetoothDevice(){
        return mBluetoothDevice;
    }
    public void setType(ArduinoDeviceType t){
        mDerivedType = t;
    }
    public ArduinoDeviceType getDerivedType(){
        return mDerivedType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(mBluetoothDevice,i);
        if(mDerivedType!=null){
            parcel.writeString(mDerivedType.toString());
        }
    }

    public static final Parcelable.Creator<ArduinoDevice> CREATOR = new Parcelable.Creator<ArduinoDevice>() {

        @Override
        public ArduinoDevice createFromParcel(Parcel source) {
            return new ArduinoDevice(source);
        }

        @Override
        public ArduinoDevice[] newArray(int size) {
            return new ArduinoDevice[size];
        }
    };
}
