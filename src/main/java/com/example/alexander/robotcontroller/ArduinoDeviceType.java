package com.example.alexander.robotcontroller;

/**
 * Created by Alexander on 24.05.2016.
 */
public enum ArduinoDeviceType {
    ROBOT,DEVICE;
    @Override
    public String toString(){
        if(this == ROBOT){
                return "Robot";
        } else {
            return "Device";
        }
    }
}

