package com.example.alexander.robotcontroller;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Alexander on 24.05.2016.
 */
public class ArduinoDeviceArrayAdapter extends ArrayAdapter<ArduinoDevice> {
    public ArduinoDeviceArrayAdapter(Context context, ArrayList<ArduinoDevice> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArduinoDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_arduino_item_layout, parent, false);
        }
        TextView deviceName = (TextView) convertView.findViewById(R.id.arduinoDeviceName);
        TextView deviceAddres = (TextView) convertView.findViewById(R.id.arduinoDeviceMac);
        deviceName.setText(device.getName());
        deviceAddres.setText(device.getAddress());
        return convertView;
    }
}
