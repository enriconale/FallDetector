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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
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

    // Space for the last seconds of acceleration integral
    private double[] mLastSecondIntegrals = {0,0,0};
    private double[] m2ndLastSecondIntegrals = {0,0,0};
    private double[] m3rdLastSecondIntegrals = {0,0,0};

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

    // for debug-reasons the data can be written to a file
    private final static boolean WRITE_FILE = true;
    private final static double FALLBACK_THRESHOLD = 30;
    private OutputStreamWriter fileOut;

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

        if(WRITE_FILE){
            try {
                java.io.FileOutputStream fOut = mContext.openFileOutput("accelerometerdata.txt",
                        Context.MODE_WORLD_READABLE);
                fileOut = new java.io.OutputStreamWriter(fOut);
            } catch (IOException e){
                System.out.println("Writing to file failed: "+ e);
            }
        }
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

            if (WRITE_FILE){
                try {
                    String text = i+" "+event.timestamp+" "+event.values[0]+" "+mCurrentGravityX+" "+event.values[1]+" "
                            +mCurrentGravityY+" "+event.values[2]+" "+mCurrentGravityZ+"\n";
                    fileOut.write(text.toCharArray());
                } catch (IOException e) {
                    System.err.println(e.toString());
                }
            }
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
            long end = xBuffer.getLastIndex(0);
            xBuffer.setLastIndex(i);
            // tests every triplet of accelerometer data for acceleration over 20 m/(s^2)
            for(; j > end; j--){
                double totalAcc =Math.sqrt(
                        xBuffer.getAccelerationBuffer().readOne(j)
                                * xBuffer.getAccelerationBuffer().readOne(j)
                                + yBuffer.getAccelerationBuffer().readOne(j)
                                * yBuffer.getAccelerationBuffer().readOne(j)
                                + zBuffer.getAccelerationBuffer().readOne(j)
                                * zBuffer.getAccelerationBuffer().readOne(j));
                if (totalAcc > FALLBACK_THRESHOLD)
                    return true;
            }
            return false;
        }

        // Builds integral of movement in the last two seconds
        m3rdLastSecondIntegrals = Arrays.copyOf(m2ndLastSecondIntegrals,3);
        m2ndLastSecondIntegrals = Arrays.copyOf(mLastSecondIntegrals,3);

        // As we know we are called by a timer the interval is one second
        mLastSecondIntegrals[0] = xBuffer.requestNextIntegral(i);
        mLastSecondIntegrals[1] = yBuffer.requestNextIntegral(i);
        mLastSecondIntegrals[2] = zBuffer.requestNextIntegral(i);

        if(WRITE_FILE) {
            try {
                String str = "x" + mLastSecondIntegrals[0] + "\n";
                fileOut.write(str.toCharArray());
                str = "y" + mLastSecondIntegrals[1] + "\n";
                fileOut.write(str.toCharArray());
                str = "z" + mLastSecondIntegrals[2] + "\n";
                fileOut.write(str.toCharArray());
            } catch (IOException e){
                System.err.println(e.toString());
            }
        }

        // If there was no significant movement in the last two seconds,
        // looks if the was movement in direction of gravity in the second before
        // The number 20 is an empirical value
        double lastTwoSeconds = mLastSecondIntegrals[0] + mLastSecondIntegrals[1]
                + mLastSecondIntegrals[2] + m2ndLastSecondIntegrals[0] +
                m2ndLastSecondIntegrals[2] + m2ndLastSecondIntegrals[2];
        if(lastTwoSeconds < 20) {
            // Weight movement with gravity to reveal downward movement
            double weightedIntegral = Math.sqrt(Math.abs(
                    m3rdLastSecondIntegrals[0] * mCurrentGravityX
                            * m3rdLastSecondIntegrals[0] * mCurrentGravityX +
                            m3rdLastSecondIntegrals[1] * mCurrentGravityY
                                    * m3rdLastSecondIntegrals[1] * mCurrentGravityY +
                            m3rdLastSecondIntegrals[2] * mCurrentGravityY
                                    * m3rdLastSecondIntegrals[2] * mCurrentGravityY));
            // Empirical value of a fall (ca. 40 cm)
            if(WRITE_FILE) {
                try {
                    String str = "x" + m3rdLastSecondIntegrals[0] + "\n";
                    fileOut.write(str.toCharArray());
                    str = "y" + m3rdLastSecondIntegrals[1] + "\n";
                    fileOut.write(str.toCharArray());
                    str = "z" + m3rdLastSecondIntegrals[2] + "\n";
                    fileOut.write(str.toCharArray());
                    str = "Weighted" + weightedIntegral + "\n";
                    fileOut.write(str.toCharArray());
                } catch (IOException e){
                    System.err.println(e.toString());
                }
            }
            if (weightedIntegral > 400) {
                if(WRITE_FILE) {
                    try {
                        String str = "FALL DETECTED HERE\n";
                        fileOut.write(str.toCharArray());
                    } catch (IOException e){
                        System.err.println(e.toString());
                    }
                }
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
        long begin = xBuffer.getLastIndex(1);
        long end = xBuffer.getLastIndex(0);

        // in fallback, a fall in the last second is revealed
        // in normal mode a fall between two and three seconds ago is revealed
        if(mFallbackMode){
            mLastFallIndex = (i + xBuffer.getLastIndex(1)) / 2;
            try{Thread.sleep(600);} catch (Exception e) {}
        }
        else {
            begin = xBuffer.getLastIndex(3);
            end = xBuffer.getLastIndex(2);
            mLastFallIndex = (xBuffer.getLastIndex(2) - xBuffer.getLastIndex(3)) / 2;
        }
        Toast.makeText(mContext, R.string.register_fall_event , Toast.LENGTH_LONG).show();

        // give the FallObjectCreator the acceleration data and the apiClient for location
        FallObjectCreator foc = new FallObjectCreator(mTimeBuffer, xBuffer.getAccelerationBuffer(),
                yBuffer.getAccelerationBuffer(), zBuffer.getAccelerationBuffer(),
                mContext, mGoogleApiClient,begin,end);

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
