package com.example.alexander.robotcontroller;

/**
 * Created by Alexander on 07.06.2016.
 */
public class AlgorithmRequiredPointData {
    private float mLength;
    private float mAzimuth;
    private float mPitch;
    public AlgorithmRequiredPointData(float a) {
        mAzimuth = a;
    }
    public AlgorithmRequiredPointData(float a, float l) {
        mAzimuth = a;
        mLength = l;
    }
    public void calculateLength(float pitch, float height) {
        mLength = height / ((float) (Math.cos(Math.toRadians(pitch))));
        mPitch = pitch;
    }

    public float getAzimuth() {
        return mAzimuth;
    }

    public float getLength() {
        return mLength;
    }
    public float getPitch() {
        return mPitch;
    }
}
