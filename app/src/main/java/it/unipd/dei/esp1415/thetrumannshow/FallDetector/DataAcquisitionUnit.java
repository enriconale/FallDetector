package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.Activity;
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

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Eike Trumann on 29.03.15.
 * all rights reserved
 *
 * The DataAcquisitionUnit is the core part of the fall detection.
 * It requests the datasets from the Android framework and includes the fall detection algorithm.
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
     * Set up ans start data acquisition
     * @param c Application context of the FallDetector application
     */
    DataAcquisitionUnit(Context c){
        // set up data-buffers
        xBuffer = new DifferentialBuffer(samples, mTimeBuffer);
        yBuffer = new DifferentialBuffer(samples, mTimeBuffer);
        zBuffer = new DifferentialBuffer(samples, mTimeBuffer);


        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(c);
        String sensorRatePreference = mSharedPref.getString(SettingsActivity
                .PREF_ACCELEROMETER_RATE, "normal");

        // set sample rate according to user preference
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

        // activate fallback to shock detection for devices without gravity sensor
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getSensorList(Sensor.TYPE_GRAVITY).isEmpty()){
            mFallbackMode = true;
        }

        // register gravity listener at low frequency (10 Hz) as comparison for accelerometer
        if (!mFallbackMode) {
            mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            mSensorManager.registerListener(this, mGravity, 100000);
        }

        // wait to make sure the first gravity sample is ready
        try{Thread.sleep(200);} catch (Exception e) {}

        // register accelerometer listener
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, mChosenSensorRate);

        //Location
        buildGoogleApiClient(c);
        mGoogleApiClient.connect();

        // set up timer to execute fall detection every second, delay of 3 seconds
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isFall()) {
                    // the fall method must be run on the UiThread to allow opening email-activity
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

    /**
     * Saves gravity data to temporary storage
     * Saves accelerometer data to buffer
     * @param event Event carrying accelerometer / gravity data
     */
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

    /**
     * Fall detection method.
     * Fallback: fall is detected if there is any acceleration above 20 m/(s^2)
     * Normal: First looks if integral over last two seconds does not reveal significant motion,
     * than tests, if there was significant motion in the second before.
     * @return true if a fall was detected
     */
    private boolean isFall(){
        if (mFallbackMode){
            long j = i;
            // one second sampling interval
            long end = j - (1000000000/xBuffer.approximateIntervalNanos());
            // tests every triplet of accelerometer data for acceleration above 20 m/(s^2)
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

        // Builds integral of movement in the last two seconds
        double xIntegral = xBuffer.requestIntegralByTime(2000000000, 0);
        double yIntegral = yBuffer.requestIntegralByTime(2000000000, 0);
        double zIntegral = zBuffer.requestIntegralByTime(2000000000, 0);
        System.out.println("First: "+(xIntegral + yIntegral + zIntegral));
        // If there was no significant movement in the last two seconds,
        // looks if the was movement in direction of gravity in the second before
        // The number 15 is an empirical value
        if((xIntegral + yIntegral + zIntegral) < 15) {
            xIntegral = xBuffer.requestIntegralByTime(1000000000L, 2000000000);
            yIntegral = yBuffer.requestIntegralByTime(1000000000L, 2000000000);
            zIntegral = zBuffer.requestIntegralByTime(1000000000L, 2000000000);
            // Weight movement with gravity to reveal downward movement
            // if the sign of integral ang gravity are different 0 is added.
            double weightedIntegral = Math.sqrt(Math.abs(
                    ((xIntegral * mCurrentGravityX) > 0.0 ?
                        xIntegral * mCurrentGravityX * xIntegral * mCurrentGravityX : 0) +
                    (yIntegral * mCurrentGravityY > 0.0 ?
                        yIntegral * mCurrentGravityY * yIntegral * mCurrentGravityY : 0) +
                    (zIntegral * mCurrentGravityZ > 0.0 ?
                        zIntegral * mCurrentGravityZ * zIntegral * mCurrentGravityZ : 0)));
            // Empirical value of a fall (ca. 40 cm)
            System.out.println("Weighted: "+weightedIntegral);
            if (weightedIntegral > 50) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a fall-object to be saved to the data-storage.
     * Must be run on the UI thread to function properly.
     */
    private void fall(){
        // in fallback, a fall in the last second is revealed
        // in normal mode a fall between two and three seconds ago is revealed
        if(mFallbackMode){
            mLastFallIndex = i - (500000000L / xBuffer.approximateIntervalNanos());
            try{wait(100);} catch (Exception e) {}
        }
        else {
            mLastFallIndex = i - (2500000000L / xBuffer.approximateIntervalNanos());
        }
        Toast.makeText(mContext, R.string.register_fall_event , Toast.LENGTH_LONG).show();

        // give the FallObjectCreator the acceleration data and the apiClient for location
        FallObjectCreator foc = new FallObjectCreator(mTimeBuffer, xBuffer.getAccelerationBuffer(),
                yBuffer.getAccelerationBuffer(), zBuffer.getAccelerationBuffer(),
                mContext, mGoogleApiClient, mLastFallIndex);

        // start FallObjectCreator in new thread to not block application during location search
        Thread focThread = new Thread(foc);
        focThread.run();
    }

    /**
     * Detaches the listeners when the session is closed
     */
    void detach(){
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    /**
     * Resumes the listeners after a break
     */
    void resume() {
        mSensorManager.registerListener(this, mAccelerometer, mChosenSensorRate);
        mSensorManager.registerListener(this, mGravity, 100000);
        mGoogleApiClient.connect();
    }

    /**
     * connection to proprietary Google Play Services
     * used for FusedLocationProvider when a fall was detected
     */
    protected synchronized void buildGoogleApiClient(Context c) {
        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /*
     * Useless methods required to implement interfaces correctly
     */

    @Override
    public void onConnected(Bundle bundle) { }

    @Override
    public void onConnectionSuspended(int i) { }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) { }

    @Override
    public void onAccuracyChanged(Sensor sensor, int Accuracy){ }
}
