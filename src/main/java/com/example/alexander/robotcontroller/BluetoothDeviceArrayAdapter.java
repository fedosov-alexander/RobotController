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
 * Created by Alexander on 22.05.2016.
 */
public class BluetoothDeviceArrayAdapter  extends ArrayAdapter<BluetoothDevice> {
    public BluetoothDeviceArrayAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        super(context, 0, devices);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDevice device = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
        }
        TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
        TextView deviceAddres = (TextView) convertView.findViewById(R.id.deviceMAC);
        deviceName.setText(device.getName());
        deviceAddres.setText(device.getAddress());
        return convertView;
    }
}