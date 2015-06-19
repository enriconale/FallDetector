package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved.
 *
 * The DifferentialBuffer consists of two circular ring buffers that save gravity and accelerometer
 * data.
 * It can compute integrals over the datasets to allow conversion from acceleration to movement.
 */
public class DifferentialBuffer {
    private final FloatRingBuffer mAccelerationBuffer;
    private final FloatRingBuffer mGravityBuffer;
    private final LongRingBuffer mTimeBuffer;

    // An acceleration under 1 m/(s^2) will not be included, this is a noise filter.
    private final double mAccelerationThreshold = 1;

    /**
     * Set up the buffer at the given capacity.
     * @param capacity real capacity of the circular buffers
     * @param timeBuffer shared, necessarily synchronised buffer for timestamps/
     */
    public DifferentialBuffer(int capacity, LongRingBuffer timeBuffer){
        mAccelerationBuffer = new FloatRingBuffer(capacity);
        mGravityBuffer = new FloatRingBuffer(capacity);
        mTimeBuffer = timeBuffer;
    }

    /**
     * Add a dataset to the buffer.
     * This must be done together with the shared time buffer.
     * @param acceleration acceleration data (m/(s^2))
     * @param gravity gravity data (m/(s^2))
     */
    public void submitData(float acceleration, float gravity){
        mAccelerationBuffer.insert(acceleration);
        mGravityBuffer.insert(gravity);
    }

    /**
     * Get an integral over the given sample interval weighted with timestamp difference.
     * @param samples number of samples to include
     * @param offset offset from the newest dataset
     * @return the requested integral
     */
    public double requestIntegral(int samples, int offset){
        double integral = 0;
        long j = mTimeBuffer.getCurrentPosition() - offset;
        long termination = j - samples;
        for(;j >= 0 && j > termination; j--){
            double cleanAcceleration = mAccelerationBuffer.readOne(j) - mGravityBuffer.readOne(j);
            if(cleanAcceleration > mAccelerationThreshold
                    || cleanAcceleration < -mAccelerationThreshold){
                long intervalNanos = mTimeBuffer.readOne(j) - mTimeBuffer.readOne(j-1);
                integral += Math.abs(cleanAcceleration) * ((double) intervalNanos / 1000000000);
            }
        }

        return integral;
    }

    /**
     * Request the integral of acceleration data in a given time interval.
     * @param interval length of interval in nanoseconds
     * @param offset offset from newest dataset in nanoseconds
     * @return integral over the linear acceleration in the given interval
     */
    public double requestIntegralByTime(long interval,long offset){
        long intervalSize = approximateIntervalNanos();
        return requestIntegral((int) (interval/intervalSize), (int) (offset/intervalSize));
    }

    /**
     * get the approximate time interval between to samples in the buffer
     * @return the approximate time between two samples in nanoseconds
     */
    public long approximateIntervalNanos(){
        long diff = mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition()) -
                mTimeBuffer.readOne(mTimeBuffer.getCurrentPosition() - 99);
        return diff / 99;
    }

    /**
     * get the underlying acceleration buffer for visualization purposes
     * @return the FloatRingBuffer containing the acceleration data
     */
    public FloatRingBuffer getAccelerationBuffer(){
        return mAccelerationBuffer;
    }
}
