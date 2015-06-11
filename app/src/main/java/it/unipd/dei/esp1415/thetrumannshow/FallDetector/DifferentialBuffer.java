package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved.
 */
public class DifferentialBuffer {
    private final FloatRingBuffer mAccelerationBuffer;
    private final FloatRingBuffer mGravityBuffer;
    private final LongRingBuffer mTimeBuffer;

    private final double mAccelerationThreshold = 1;

    public DifferentialBuffer(int capacity, LongRingBuffer timeBuffer){
        mAccelerationBuffer = new FloatRingBuffer(capacity);
        mGravityBuffer = new FloatRingBuffer(capacity);
        mTimeBuffer = timeBuffer;
    }

    public void submitData(float acceleration, float gravity){
        mAccelerationBuffer.insert(acceleration);
        mGravityBuffer.insert(gravity);
    }

    public double requestIntegral(int samples, int offset){
        double integral = 0;
        long j = mTimeBuffer.getCurrentPosition() - offset;

        for(;j >= 0 && j > j - samples; j--){
            double cleanAcceleration = mAccelerationBuffer.readOne(j) - mGravityBuffer.readOne(j);
            if(cleanAcceleration > mAccelerationThreshold
                    || cleanAcceleration < -mAccelerationThreshold){
                long intervalNanos = mTimeBuffer.readOne(j) - mTimeBuffer.readOne(j-1);
                integral += cleanAcceleration * ((double) intervalNanos / 1000000000);
            }
         }

        return integral;
    }

    public double requestIntegralByTime(long interval,long offset){
        long intervalSize = approximateIntervalNanos();
        return requestIntegral((int) (interval/intervalSize), (int) (offset/intervalSize));
    }

    private long approximateIntervalNanos(){
        long diff = mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition()) -
                mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition() - 99);
        return diff / 99;
    }

    public FloatRingBuffer getAccelerationBuffer(){
        return mAccelerationBuffer;
    }
}
