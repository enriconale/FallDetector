package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Eike Trumann on 29.03.15.
 * all rights reserved
 */
public class DataAcquisitionUnit implements SensorEventListener {
    private static SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Context mContext;

    private final static int samples = 60;

    private long[] timeBuffer = new long[samples];
    private float[] xBuffer = new float[samples];
    private float[] yBuffer = new float[samples];
    private float[] zBuffer = new float[samples];

    private int i = 0;

    DataAcquisitionUnit(Context c){
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mContext = c;
    }

    public void onAccuracyChanged(Sensor sensor, int Accuracy){
    }

    public void onSensorChanged(SensorEvent event){
        timeBuffer[i % samples] = event.timestamp;
        xBuffer[i % samples] = event.values[0];
        yBuffer[i % samples] = event.values[1];
        zBuffer[i % samples] = event.values[2];

        i++;

        if (i % samples == 0){
            if(isFall()){
                fall();
            }
            // writeToFile("autoSave"+i/samples+".csv");
        }
    }

    void writeToFile(String filename){
        try {
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
        }
    }

    // dummy implementation of fall detection
    private boolean isFall(){
        for(int j = 0; j < samples; j++) {
            double acc = Math.sqrt(xBuffer[j] * xBuffer[j] + yBuffer[j] * yBuffer[j]
                    + zBuffer[j] * zBuffer[j]);
            if (acc > 20.0 || acc < 3.0)
                return true;
        }
        return false;
    }

    private void fall(){
        Toast.makeText(mContext, "REGISTERED FALL EVENT", Toast.LENGTH_LONG).show();
    }

    void detach(){
        mSensorManager.unregisterListener(this);
    }
}
