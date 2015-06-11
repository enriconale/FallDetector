package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved.
 */
public class DifferentialBuffer {
    private final FloatRingBuffer mAccelerationBuffer;
    private final FloatRingBuffer mGravityBuffer;
    private final LongRingBuffer mTimeBuffer;

    private final double mIntervalMicros;
    private final int mSamples;

    private double mFloatingIntegral;

    public DifferentialBuffer(int capacity, int samples, double intervalMicros){
        mAccelerationBuffer = new FloatRingBuffer(capacity);
        mGravityBuffer = new FloatRingBuffer(capacity);
        mTimeBuffer = new LongRingBuffer(capacity);
        this.mIntervalMicros = intervalMicros;
        this.mSamples = samples;
    }

    public void submitData(float acceleration, float gravity, long timestamp){
        mAccelerationBuffer.insert(acceleration);
        mGravityBuffer.insert(gravity);
        mTimeBuffer.insert(timestamp);
        long oldBufferPosition = mAccelerationBuffer.getCurrentPosition() - mSamples;
        long intervalNanos = timestamp - mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition()-1);
        long oldIntervalNanos = mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition() - mSamples)
                - mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition() - mSamples - 1);
        mFloatingIntegral += (Math.abs(acceleration-gravity))
                * ((double) intervalNanos / 1000000000);
        mFloatingIntegral -= (Math.abs((double)(mAccelerationBuffer.readOne(oldBufferPosition)
                - mGravityBuffer.readOne(oldBufferPosition))))
                * ((double) oldIntervalNanos / 1000000000);
    }

    public FloatRingBuffer getAccelerationBuffer(){
        return mAccelerationBuffer;
    }

    public float readOne(long position){
        return mAccelerationBuffer.readOne(position);
    }

    public double getFloatingIntegral(){
        return mFloatingIntegral;
    }
}
