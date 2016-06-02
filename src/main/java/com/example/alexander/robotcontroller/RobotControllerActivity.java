package com.example.alexander.robotcontroller;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

public class RobotControllerActivity extends Activity implements SensorEventListener {
    //Default UUID
    private static final UUID Default_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int CALIBRATION_ITERATIONS = 50;

    private static final int REQUEST_CONNECT_TO_DEVICE = 1;
    private static final int REQUEST_CONNECT_TO_ROBOT = 2;
    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mAccelerometer;
    private TextView mCurrentHeadingTextView;
    private TextView mCurrentAngleTextView;
    private TextView mCurrentHeight;
    private Button mSetPositionButton;
    private ArrayList<ArduinoDevice> mDevices;
    private Device mDevice;
    private Robot mRobot;
    private float[] mMagneticData = new float[3];
    private float[] mAccelerometerData = new float[3];
    private float[] mOrientation = new float[3];
    private float[] mR = new float[16];
    private float[] mI = new float[16];
    private int mIterations = 0;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrentHeadingTextView = (TextView) findViewById(R.id.currentHeadingTextView);
        mCurrentAngleTextView = (TextView) findViewById(R.id.currentAngleTextView);
        mCurrentHeight = (TextView) findViewById(R.id.currentHeight);
        mSetPositionButton = (Button) findViewById(R.id.setpPositionButton);
        mSetPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RCActivity", "Oclick called");
                if (mDevice != null) {
                    try {
                        if(!mDevice.isConnected()){
                            mDevice.connect(Default_UUID);
                        }
                        mDevice.write((byte) 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mDevices = getIntent().getExtras().getParcelableArrayList("ArduinoDevices");
        for (int i = 0; (i < mDevices.size())&&(i<2); i++) {
            switch (mDevices.get(i).getDerivedType()) {
                case DEVICE: {
                    mDevice = new Device(mDevices.get(i).getBluetoothDevice(), mHandler);
                    break;
                }
                case ROBOT: {
                    mRobot = new Robot(mDevices.get(i).getBluetoothDevice());
                    break;
                }
                default: {
                    break;
                }
            }
        }
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mCurrentHeight.setText("current height = " + msg.getData().getFloat("height"));
            }
        };
        if(mRobot!=null){
            connectToBluetoothDevice(mRobot, REQUEST_CONNECT_TO_ROBOT);
        }
        if(mDevice!=null){
            connectToBluetoothDevice(mDevice, REQUEST_CONNECT_TO_DEVICE);
        }
    }

    private void connectToBluetoothDevice(ArduinoDevice device,int code) {
        String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        Intent intent = new Intent(ACTION_PAIRING_REQUEST);
        String EXTRA_DEVICE = "android.bluetooth.device.extra.DEVICE";
        intent.putExtra(EXTRA_DEVICE,device.getBluetoothDevice());
        String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
        int PAIRING_VARIANT_PIN = 0;
        intent.putExtra(EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        try {
            // mDevice.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //mDevice.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        float[] data;
        if (type == Sensor.TYPE_ACCELEROMETER) {
            data = mAccelerometerData;
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            data = mMagneticData;
        } else {
            return;
        }
        for (int i = 0; i < 3; i++) {
            data[i] = event.values[i];
        }
        SensorManager.getRotationMatrix(mR, mI, mAccelerometerData, mMagneticData);
// some test code which will be used/cleaned up before we ship this.
//        SensorManager.remapCoordinateSystem(mR,
//                SensorManager.AXIS_X, SensorManager.AXIS_Z, mR);
//        SensorManager.remapCoordinateSystem(mR,
//                SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mR);
        SensorManager.getOrientation(mR, mOrientation);
        float incl = SensorManager.getInclination(mI);
        mIterations++;
        if (mIterations > CALIBRATION_ITERATIONS) {
            final float rad2deg = (float) (180.0f / Math.PI);
            mIterations = 0;
            mCurrentHeadingTextView.setText("Current heading: " + (int) (mOrientation[0] * rad2deg) + " degrees");
           /* Log.d("Compass", "yaw: " + (int)(mOrientation[0]*rad2deg) +
                    "  pitch: " + (int)(mOrientation[1]*rad2deg) +
                    "  roll: " + (int)(mOrientation[2]*rad2deg) +
                    "  incl: " + (int)(incl*rad2deg)
            );*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}


