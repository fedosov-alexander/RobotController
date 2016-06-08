package com.example.alexander.robotcontroller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class RobotControllerActivity extends Activity implements SensorEventListener {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final int CALIBRATION_ITERATIONS = 50;
    private static final int UPDATE_ROBOT_POSITION = 1;
    private static final int UPDATE_DESTINATION_POINT = 2;
    private static final float RADIANS_TO_DEGREES = (float) (180.0f / Math.PI);

    private SensorManager mSensorManager;
    private Sensor mMagneticSensor;
    private Sensor mAccelerometer;
    private float[] mMagneticData = new float[3];
    private float[] mAccelerometerData = new float[3];
    private float[] mOrientation = new float[3];
    private float[] mR = new float[16];
    private float[] mI = new float[16];
    private int mIterations = 0;

    private TextView mCurrentHeadingTextView;
    private TextView mCurrentAngleTextView;
    private TextView mRobotHeadingTextView;
    private TextView mRobotAngleTextView;
    private TextView mDestinationHeadingTextView;
    private TextView mDestinationAngleTextView;
    private float mCurrentHeading;
    private Button mSetRobotPositionButton;
    private Button mSetDestinationPointButton;
    private Button mSendDataButton;

    private ArrayList<ArduinoDevice> mDevices;

    private Device mDevice = null;
    private Robot mRobot = null;
    private AlgorithmRequiredPointData mRobotPositionData;
    private AlgorithmRequiredPointData mDestinationPointData;

    private Handler mMessageHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrentHeadingTextView = (TextView) findViewById(R.id.currentHeadingTextView);
        mCurrentAngleTextView = (TextView) findViewById(R.id.currentAngleTextView);
        mRobotHeadingTextView = (TextView) findViewById(R.id.headingOnRobotTextView);
        mRobotAngleTextView = (TextView) findViewById(R.id.angleOnRobotTextView);
        mDestinationHeadingTextView = (TextView) findViewById(R.id.headingOnDestinationTextView);
        mDestinationAngleTextView = (TextView) findViewById(R.id.angleOnDestinationTextView);
        mSetRobotPositionButton = (Button) findViewById(R.id.setRobotPositionButton);
        mSetRobotPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDevice != null) {
                    try {
                        if (!mDevice.isConnected()) {
                            mDevice.connect(MY_UUID, mMessageHandler);
                        }
                        mDevice.write((byte) 11);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    updateRobotPosition(100);
                }
            }
        });
        mSetDestinationPointButton = (Button) findViewById(R.id.setDestinationPointButton);
        mSetDestinationPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDevice != null) {
                    try {
                        if (!mDevice.isConnected()) {
                            mDevice.connect(MY_UUID, mMessageHandler);
                        }
                        mDevice.write((byte) 12);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    updateDestinationPoint(100);
                }
            }
        });
        mSendDataButton = (Button) findViewById(R.id.sendDataButton);
        mSendDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mRobot != null) && (mRobotPositionData != null) && (mDestinationPointData != null)) {
                    AlgorithmRequiredPointData d = getRobotControlData(mRobotPositionData, mDestinationPointData);
                    mRobot.write(d.getAzimuth(), d.getLength());
                }
            }
        });
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mDevices = getIntent().getExtras().getParcelableArrayList("ArduinoDevices");
        for (int i = 0; (i < mDevices.size()) && (i < 2); i++) {
            switch (mDevices.get(i).getDerivedType()) {
                case DEVICE: {
                    mDevice = new Device(mDevices.get(i).getBluetoothDevice());
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

        mMessageHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Log.d("RCActivity", "HandleMessage called, msg.waht = " + msg.what);
                Float length = (Float) msg.obj;
                switch (msg.what) {
                    case UPDATE_ROBOT_POSITION: {
                        updateRobotPosition(length);
                        break;
                    }
                    case UPDATE_DESTINATION_POINT: {
                        updateDestinationPoint(length);
                        break;
                    }
                    default: {
                        break;
                    }
                }

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
        if ((mRobot != null)) {
            mRobot.connect(MY_UUID);
        }
        if ((mDevice != null) && (!mDevice.isConnected())) {
            mDevice.connect(MY_UUID, mMessageHandler);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        try {
            mDevice.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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
        SensorManager.getOrientation(mR, mOrientation);
        mIterations++;
        if (mIterations > CALIBRATION_ITERATIONS) {
            mIterations = 0;
            mCurrentHeading = mOrientation[0] * RADIANS_TO_DEGREES;
            mCurrentHeading += (mCurrentHeading > 0 ? 0 : 360);
            mCurrentHeadingTextView.setText("Current heading: " + (int) (mCurrentHeading) + " degrees");
            mCurrentAngleTextView.setText("Current angle: " + (int) (mOrientation[1] * RADIANS_TO_DEGREES) + " degrees");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private AlgorithmRequiredPointData getRobotControlData(AlgorithmRequiredPointData robotPosition, AlgorithmRequiredPointData destinationPoint) {
        float angle = Math.abs(robotPosition.getAzimuth() - destinationPoint.getAzimuth());
        if (angle < 5) {
            float heading;
            angle = Math.abs(robotPosition.getPitch() - destinationPoint.getPitch());
            if (robotPosition.getPitch() > destinationPoint.getPitch()) {
                heading = robotPosition.getAzimuth();
            } else {
                heading = robotPosition.getAzimuth();
                heading += 180;
                if (heading > 360) {
                    heading -= 360;
                }
            }
            return new AlgorithmRequiredPointData(heading,
                    cosineTheorem(robotPosition.getLength(), angle, destinationPoint.getLength()));
        }
        if (Math.abs(angle - 180) < 5) {
            angle = 180 - robotPosition.getPitch() - destinationPoint.getPitch();
            return new AlgorithmRequiredPointData(destinationPoint.getAzimuth(),
                    cosineTheorem(robotPosition.getLength(), angle, destinationPoint.getLength()));
        }
        double robotPositionHeadingInRadians = Math.toRadians(robotPosition.getAzimuth());
        double destinationPointHeadingInRadians = Math.toRadians(destinationPoint.getAzimuth());
        float length = cosineTheorem(robotPosition.getLength(), angle, destinationPoint.getLength());
        double xDestination, yDestination, xRobot, yRobot;
        xDestination = destinationPoint.getLength() * Math.cos(destinationPointHeadingInRadians);
        yDestination = destinationPoint.getLength() * Math.sin(destinationPointHeadingInRadians);
        xRobot = robotPosition.getLength() * Math.cos(robotPositionHeadingInRadians);
        yRobot = robotPosition.getLength() * Math.sin(robotPositionHeadingInRadians);
        xDestination -= xRobot;
        yDestination -= yRobot;
        xDestination /= length;//stores cos of azimuth from robot position on destination point
        yDestination /= length;//stores sin of azimuth from robot position on destination point
        double azimuth;
        azimuth = Math.acos(xDestination);
        azimuth = Math.toDegrees(azimuth);
        if (yDestination < 0) {
            azimuth = 360 - azimuth;
        }
        return new AlgorithmRequiredPointData((float) azimuth, length);
    }

    private float cosineTheorem(float a, float angle, float b) {
        double result = a * a + b * b - 2 * a * b * Math.cos(Math.toRadians(angle));
        return ((float) Math.sqrt(result));
    }

    private void updateRobotPosition(float length) {
        mRobotPositionData = new AlgorithmRequiredPointData((mCurrentHeading));
        mRobotPositionData.calculateLength((mOrientation[1] * RADIANS_TO_DEGREES), length);
        mRobotHeadingTextView.setText("Heading on robot position: " + (int) (mCurrentHeading) + " degrees");
        mRobotAngleTextView.setText("Angle on robot position: " + (int) ((mOrientation[1] * RADIANS_TO_DEGREES)) + " degrees");
    }

    private void updateDestinationPoint(float length) {
        mDestinationPointData = new AlgorithmRequiredPointData((mCurrentHeading));
        mDestinationPointData.calculateLength((mOrientation[1] * RADIANS_TO_DEGREES), length);
        mDestinationHeadingTextView.setText("Heading on destination point: " + (int) (mCurrentHeading) + " degrees");
        mDestinationAngleTextView.setText("Angle on destination point: " + (int) ((mOrientation[1] * RADIANS_TO_DEGREES)) + " degrees");
    }
}


