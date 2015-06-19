package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Chronometer;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Eike Trumann on 29.03.15.
 * all rights reserved
 */
public class DataAcquisitionUnit
        implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
    {

    // Sensor manager to provide sensors for data acquistion
    private static SensorManager mSensorManager;
    // Google API-client to use proprietary fused location provider
    private GoogleApiClient mGoogleApiClient;
    // Reference to the accelerometer
    private Sensor mAccelerometer;
    // Gravity sensor to register only actual acceleration
    private Sensor mGravity;
    // Application context given from MainActivity
    private Context mContext;
    private Activity mMainActivity;
    // Sensor Rate chosen in application settings.
    private int mChosenSensorRate;

    // size of the sensor sample buffer
    private final static int samples = 1000;

    // Shared time buffer for accelerometer data
    private LongRingBuffer mTimeBuffer = new LongRingBuffer(samples);
    // Buffer for accelerometer data and gravity for comparison
    private DifferentialBuffer xBuffer;
    private DifferentialBuffer yBuffer;
    private DifferentialBuffer zBuffer;

    // temporary storage for last known gravity value
    private float mCurrentGravityX = 0;
    private float mCurrentGravityY = 0;
    private float mCurrentGravityZ = 0;

    // counter for incoming sensor data
    private long i = 0;

    // counter when last fall was detected
    private long mLastFallIndex = -1;

    // fallback-mode if there is no gravity sensor
    private boolean mFallbackMode = false;

    /**
     *
     * @param c Application context of the FallDetector application
     */
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
        if (mSensorManager.getSensorList(Sensor.TYPE_GRAVITY).isEmpty()){
            mFallbackMode = true;
        }

        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(this, mGravity, 100000);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        try{Thread.sleep(200);} catch (Exception e) {}
        mSensorManager.registerListener(this, mAccelerometer, mChosenSensorRate);


        xBuffer = new DifferentialBuffer(samples, mTimeBuffer);
        yBuffer = new DifferentialBuffer(samples, mTimeBuffer);
        zBuffer = new DifferentialBuffer(samples, mTimeBuffer);

        //Location
        buildGoogleApiClient(c);
        mGoogleApiClient.connect();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isFall()) {
                    MainActivity.getLastActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fall();
                        }
                    });
                }
            }
        }, 3000,1000);

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
        }
    }

    // dummy implementation of fall detection
    private boolean isFall(){
        if (mFallbackMode){
            long j = i;
            long end = j - (1000000000/xBuffer.approximateIntervalNanos());
            for(; j > end; j--){
                double totalAcc =Math.sqrt(
                        xBuffer.getAccelerationBuffer().readOne(j) * xBuffer.getAccelerationBuffer().readOne(j)
                        + yBuffer.getAccelerationBuffer().readOne(j) * yBuffer.getAccelerationBuffer().readOne(j)
                        + zBuffer.getAccelerationBuffer().readOne(j) * zBuffer.getAccelerationBuffer().readOne(j));
                if (totalAcc > 20)
                    return true;
            }
            return false;
        }

        double xIntegral = xBuffer.requestIntegralByTime(2000000000, 0);
        double yIntegral = yBuffer.requestIntegralByTime(2000000000, 0);
        double zIntegral = zBuffer.requestIntegralByTime(2000000000, 0);

        // System.out.println("i: "+i);
        // System.out.println("new: "+(xIntegral + yIntegral + zIntegral));
        if((xIntegral + yIntegral + zIntegral) < 20) {
            xIntegral = xBuffer.requestIntegralByTime(1000000000L, 2000000000);
            yIntegral = yBuffer.requestIntegralByTime(1000000000L, 2000000000);
            zIntegral = zBuffer.requestIntegralByTime(1000000000L, 2000000000);
            double weightedIntegral = Math.sqrt(Math.abs(
                    xIntegral * mCurrentGravityX * xIntegral * mCurrentGravityX
                    + yIntegral * mCurrentGravityY * yIntegral * mCurrentGravityX
                    + zIntegral * mCurrentGravityZ * zIntegral * mCurrentGravityZ));
            // System.out.println("weighted: "+weightedIntegral);
            if (weightedIntegral > 50) {
                return true;
            }
        }
        return false;
    }

    private void fall(){
        if(mFallbackMode){
            mLastFallIndex = i - (500000000L / xBuffer.approximateIntervalNanos());
            try{wait(100);} catch (Exception e) {}
        }
        else {
            mLastFallIndex = i - (2500000000L / xBuffer.approximateIntervalNanos());
        }
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
