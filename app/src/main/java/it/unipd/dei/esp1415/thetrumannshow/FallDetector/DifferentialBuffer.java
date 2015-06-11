package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import java.util.LinkedList;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved.
 */
public class DifferentialBuffer {
    private final FloatRingBuffer mAccelerationBuffer;
    private final FloatRingBuffer mGravityBuffer;
    private final LongRingBuffer mTimeBuffer;

    private LinkedList<FloatingIntegral> mIntegrals= new LinkedList<FloatingIntegral>();
    private final FloatingIntegral mPrincipalFloatingIntegral;

    private final double mAccelerationThreshold = 1;

    public DifferentialBuffer(int capacity, int samples, double intervalMicros){
        mAccelerationBuffer = new FloatRingBuffer(capacity);
        mGravityBuffer = new FloatRingBuffer(capacity);
        mTimeBuffer = new LongRingBuffer(capacity);
        mPrincipalFloatingIntegral = new FloatingIntegral(0,samples);
        mIntegrals.add(mPrincipalFloatingIntegral);
    }

    public void submitData(float acceleration, float gravity, long timestamp){
        mAccelerationBuffer.insert(acceleration);
        mGravityBuffer.insert(gravity);
        mTimeBuffer.insert(timestamp);

        double cleanAcceleration = Math.abs(acceleration - gravity);
        if (cleanAcceleration > mAccelerationThreshold){
            long intervalNanos = timestamp - mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition()-1);
            double distance = (Math.abs(acceleration - gravity))
                    * ((double) intervalNanos / 1000000000);

             for (FloatingIntegral f:mIntegrals){
                f.add(distance);
            }
         }

        for(FloatingIntegral f:mIntegrals) {
            long oldBufferPosition = mAccelerationBuffer.getCurrentPosition() - f.size;
            double oldCleanAcceleration = Math.abs((double) (mAccelerationBuffer.readOne(oldBufferPosition)
                    - mGravityBuffer.readOne(oldBufferPosition)));
            if (oldCleanAcceleration > mAccelerationThreshold) {
                long oldIntervalNanos = mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition() - f.size)
                        - mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition() - f.size - 1);
                f.substract(oldCleanAcceleration
                        * ((double) oldIntervalNanos / 1000000000));
            }
        }
    }

    public FloatRingBuffer getAccelerationBuffer(){
        return mAccelerationBuffer;
    }

    public float readOne(long position){
        return mAccelerationBuffer.readOne(position);
    }

    public double getFloatingIntegral(){
        return mPrincipalFloatingIntegral.getValue();
    }

    public FloatingIntegral requestTemporaryIntegral(int sampleCount){
        FloatingIntegral fInt = new FloatingIntegral(0,sampleCount);
        mIntegrals.add(fInt);
        return fInt;
    }

    public void removeFloatingIntegral(FloatingIntegral f){
        mIntegrals.remove(f);
    }
}
