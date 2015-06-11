package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Eike Trumann on 29.03.15.
 * all rights reserved
 */
public class DataAcquisitionUnit
        implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private Sensor mAccelerometer;
    private Sensor mGravity;
    private Context mContext;
    private int mChosenSensorRate;


    private final static int samples = 1000;

    private LongRingBuffer timeBuffer = new LongRingBuffer(samples);
    private DifferentialBuffer xBuffer;
    private DifferentialBuffer yBuffer;
    private DifferentialBuffer zBuffer;

    private float currentGravityX = 0;
    private float currentGravityY = 0;
    private float currentGravityZ = 0;

    private long i = 0;
    private long mLastFallIndex = -1;
    private boolean mFallPending;

    DataAcquisitionUnit(Context c){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        String sensorRatePreference = mSharedPref.getString(SettingsActivity
                .PREF_ACCELEROMETER_RATE, "normal");

        switch (sensorRatePreference) {
            case "slow":
                mChosenSensorRate = SensorManager.SENSOR_DELAY_UI;
                break;
            case "fast":
                mChosenSensorRate = SensorManager.SENSOR_DELAY_FASTEST;
                break;
            case "normal":
            default:
                mChosenSensorRate = SensorManager.SENSOR_DELAY_GAME;
        }

        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mGravity, 100000);
        try{Thread.sleep(200);} catch (Exception e) {}
        mSensorManager.registerListener(this, mAccelerometer, mChosenSensorRate);


        xBuffer = new DifferentialBuffer(samples, 500, 10000);
        yBuffer = new DifferentialBuffer(samples, 500, 10000);
        zBuffer = new DifferentialBuffer(samples, 500, 10000);

        //Location
        buildGoogleApiClient(c);
        mGoogleApiClient.connect();


        mContext = c;
    }

    public void onAccuracyChanged(Sensor sensor, int Accuracy){
    }

    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
            currentGravityX = event.values[0];
            currentGravityY = event.values[1];
            currentGravityZ = event.values[2];
        }

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            timeBuffer.insert(event.timestamp);
            xBuffer.submitData(event.values[0],currentGravityX,event.timestamp);
            yBuffer.submitData(event.values[1],currentGravityY,event.timestamp);
            zBuffer.submitData(event.values[2],currentGravityZ,event.timestamp);

            i++;

            if (i % (samples / 100) == 0) {
                if (isFall((((int) (i - (samples / 100)) % samples) - 100), ((int) (i % samples) - 100))) {
                    fall();
                }
            }
        }
    }

    // dummy implementation of fall detection
    private boolean isFall(int start, int end){
         /*for(int j = start; j < end; j++) {
            double acc = Math.sqrt(xBuffer.readOne(j) * xBuffer.readOne(j)
                    + yBuffer.readOne(j) * yBuffer.readOne(j)
                    + zBuffer.readOne(j) * zBuffer.readOne(j));
            if (acc > 25.0) {
                mLastFallIndex = j;
                double xIntegral = xBuffer.getFloatingIntegral();
                double yIntegral = yBuffer.getFloatingIntegral();
                double zIntegral = zBuffer.getFloatingIntegral();
                return true;
            }
        }
        return false;
        */
        if(mFallPending){
            double xIntegral = xBuffer.getFloatingIntegral();
            double yIntegral = yBuffer.getFloatingIntegral();
            double zIntegral = zBuffer.getFloatingIntegral();

            double integralSum = (xIntegral + yIntegral +zIntegral);

            if(integralSum < 10){
                mFallPending = false;
                return true;
            }

            mFallPending = false;
        }

        double xIntegral = xBuffer.getFloatingIntegral();
        double yIntegral = yBuffer.getFloatingIntegral();
        double zIntegral = zBuffer.getFloatingIntegral();

        double weightedIntegral = (xIntegral*currentGravityX + yIntegral*currentGravityX +zIntegral*currentGravityZ);

        if(weightedIntegral > 60) {
            mLastFallIndex = xBuffer.getAccelerationBuffer().getCurrentPosition();
            xBuffer.resetIntegral();
            yBuffer.resetIntegral();
            zBuffer.resetIntegral();
            mFallPending = true;
        }
        return false;
    }

    private void fall(){
        Toast.makeText(mContext, R.string.register_fall_event , Toast.LENGTH_LONG).show();
        FallObjectCreator foc = new FallObjectCreator(timeBuffer, xBuffer.getAccelerationBuffer(),
                yBuffer.getAccelerationBuffer(), zBuffer.getAccelerationBuffer(),
                mContext, mGoogleApiClient, mLastFallIndex);
        Thread focThread = new Thread(foc);
        focThread.run();
    }

    void detach(){
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    void resume() {
        mSensorManager.registerListener(this, mAccelerometer, mChosenSensorRate);
        mSensorManager.registerListener(this, mGravity, 100000);
        mGoogleApiClient.connect();
    }

    // connection to proprietary Google Play Services

    protected synchronized void buildGoogleApiClient(Context c) {
        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
