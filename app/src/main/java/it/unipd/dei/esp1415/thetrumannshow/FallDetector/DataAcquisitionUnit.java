package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

    private LongRingBuffer mTimeBuffer = new LongRingBuffer(samples);
    private DifferentialBuffer xBuffer;
    private DifferentialBuffer yBuffer;
    private DifferentialBuffer zBuffer;

    private float mCurrentGravityX = 0;
    private float mCurrentGravityY = 0;
    private float mCurrentGravityZ = 0;

    private long i = 0;
    private long mLastFallIndex = -1;

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


        xBuffer = new DifferentialBuffer(samples, mTimeBuffer);
        yBuffer = new DifferentialBuffer(samples, mTimeBuffer);
        zBuffer = new DifferentialBuffer(samples, mTimeBuffer);

        //Location
        buildGoogleApiClient(c);
        mGoogleApiClient.connect();


        mContext = c;
    }

    public void onAccuracyChanged(Sensor sensor, int Accuracy){
    }

    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
            mCurrentGravityX = event.values[0];
            mCurrentGravityY = event.values[1];
            mCurrentGravityZ = event.values[2];
        }

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mTimeBuffer.insert(event.timestamp);
            xBuffer.submitData(event.values[0], mCurrentGravityX);
            yBuffer.submitData(event.values[1], mCurrentGravityY);
            zBuffer.submitData(event.values[2], mCurrentGravityZ);

            i++;

            if (i % (samples / 10) == 0) {
                if (isFall()) {
                    fall();
                }
            }
        }
    }

    // dummy implementation of fall detection
    private boolean isFall(){
        double xIntegral = xBuffer.requestIntegralByTime(2000000000, 0);
        double yIntegral = xBuffer.requestIntegralByTime(2000000000, 0);
        double zIntegral = xBuffer.requestIntegralByTime(2000000000, 0);

        if(/*xIntegral + yIntegral + zIntegral) < 10*/ true) {
            xIntegral = xBuffer.requestIntegralByTime(1000000000L, 000000000);
            yIntegral = xBuffer.requestIntegralByTime(1000000000L, 000000000);
            zIntegral = xBuffer.requestIntegralByTime(1000000000L, 000000000);
            double weightedIntegral = Math.sqrt(Math.abs(
                    xIntegral * mCurrentGravityX * xIntegral * mCurrentGravityX
                    + yIntegral * mCurrentGravityY * yIntegral * mCurrentGravityX
                    + zIntegral * mCurrentGravityZ * zIntegral * mCurrentGravityZ));
            if (weightedIntegral > 10)
                return true;
        }
        return false;
    }

    private void fall(){
        Toast.makeText(mContext, R.string.register_fall_event , Toast.LENGTH_LONG).show();
        FallObjectCreator foc = new FallObjectCreator(mTimeBuffer, xBuffer.getAccelerationBuffer(),
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
