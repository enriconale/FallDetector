package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Eike Trumann on 29.03.15.
 * all rights reserved
 */
public class DataAcquisitionUnit
        implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private Sensor mAccelerometer;
    private Context mContext;

    private final static int samples = 1000;

    private LongRingBuffer timeBuffer = new LongRingBuffer(samples);
    private FloatRingBuffer xBuffer = new FloatRingBuffer(samples);
    private FloatRingBuffer yBuffer = new FloatRingBuffer(samples);
    private FloatRingBuffer zBuffer = new FloatRingBuffer(samples);

    private long i = 0;

    DataAcquisitionUnit(Context c){
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        //Location
        buildGoogleApiClient(c);
        mGoogleApiClient.connect();

        mContext = c;
    }

    public void onAccuracyChanged(Sensor sensor, int Accuracy){
    }

    public void onSensorChanged(SensorEvent event){
        timeBuffer.insert(event.timestamp);
        xBuffer.insert(event.values[0]);
        yBuffer.insert(event.values[1]);
        zBuffer.insert(event.values[2]);

        i++;

        if (i % samples == 0){
            if(isFall()){
                fall();
            }
            // writeToFile("autoSave"+i/samples+".csv");
        }
    }

    void writeToFile(String filename){
        /*try {
            java.io.FileOutputStream fOut = mContext.openFileOutput(filename,
                    Context.MODE_WORLD_READABLE);
            java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fOut);

            for(int j = 0; j < samples; j++)
            {
                osw.write(timeBuffer[j]+"; "+xBuffer[j]+"; "+yBuffer[j]+"; "+zBuffer[j]+";\n");
            }

            osw.close();
        }
        catch (IOException e){
            System.out.println("Writing to file failed: "+ e);
        } */
    }

    // dummy implementation of fall detection
    private boolean isFall(){
        for(int j = 0; j < samples; j++) {
            double acc = Math.sqrt(xBuffer.readOne(j) * xBuffer.readOne(j)
                    + yBuffer.readOne(j) * yBuffer.readOne(j)
                    + zBuffer.readOne(j) * zBuffer.readOne(j));
            if (acc > 20.0 || acc < 3.0)
                return true;
        }
        return false;
    }

    private void fall(){
        Location loc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (loc != null)
            Toast.makeText(mContext, "REGISTERED FALL EVENT at "+ loc.toString(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(mContext, "REGISTERED FALL EVENT", Toast.LENGTH_LONG).show();
    }

    void detach(){
        mSensorManager.unregisterListener(this);
    }

    long[] getSurroundingSecond(long index){
        long nanosecond = timeBuffer.readOne(index);
        long j = index;
        for(; timeBuffer.readOne(index) > (nanosecond - 500000000); j--);
        long begin = j;

        j = index;
        for(; timeBuffer.readOne(index) < (nanosecond + 500000000); j++);
        long end = j;
        return new long[]{begin,end};
    }

    long[] getSurroundingSecondIndex(long nanosecond){
        return getSurroundingSecondIndex(timeBuffer.getPosition(nanosecond));
    }

    private Fall constructFallObject(int index){
        long[] interval = getSurroundingSecond(index);
        float [] xArr = xBuffer.readRange(interval[0],interval[1]);
        float [] yArr = yBuffer.readRange(interval[0],interval[1]);
        float [] zArr = zBuffer.readRange(interval[0],interval[1]);

        return new Fall("",new java.util.Date(), null ,xArr,yArr,zArr);
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

    protected void startLocationUpdates() {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


}
