package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved.
 * <p>
 * The DifferentialBuffer consists of two circular ring buffers that save gravity and accelerometer
 * data.
 * It can compute integrals over the datasets to allow conversion from acceleration to movement.
 */
public class DifferentialBuffer {
    private final FloatRingBuffer mAccelerationBuffer;
    private final FloatRingBuffer mGravityBuffer;
    private final LongRingBuffer mTimeBuffer;
    private long[] mLastI = {0, 0, 0, 0};

    // An acceleration under 1 m/(s^2) will not be included, this is a noise filter.
    private final double mAccelerationThreshold = 1;

    /**
     * Set up the buffer at the given capacity.
     *
     * @param capacity   real capacity of the circular buffers
     * @param timeBuffer shared, necessarily synchronised buffer for timestamps/
     */
    public DifferentialBuffer(int capacity, LongRingBuffer timeBuffer) {
        mAccelerationBuffer = new FloatRingBuffer(capacity);
        mGravityBuffer = new FloatRingBuffer(capacity);
        mTimeBuffer = timeBuffer;
    }

    /**
     * Add a dataset to the buffer.
     * This must be done together with the shared time buffer.
     *
     * @param acceleration acceleration data (m/(s^2))
     * @param gravity      gravity data (m/(s^2))
     */
    public void submitData(float acceleration, float gravity) {
        mAccelerationBuffer.insert(acceleration);
        mGravityBuffer.insert(gravity);
    }

    /**
     * Get the integral of the linear acceleration from last i to the new one
     * Best used with a timer to guarantee interval length.
     *
     * @param i end of the integral;
     * @return an integr
     */
    public double requestNextIntegral(long i) {
        mLastI[3] = mLastI[2];
        mLastI[2] = mLastI[1];
        mLastI[1] = mLastI[0];
        mLastI[0] = i;

        return requestIntegral((int) (mLastI[0] - mLastI[1]), 0);
    }

    /**
     * Get an integral over the given sample interval weighted with timestamp difference.
     *
     * @param samples number of samples to include
     * @param offset  offset from the newest dataset
     * @return the requested integral
     */
    public double requestIntegral(int samples, int offset) {
        double integral = 0;
        long j = mTimeBuffer.getCurrentPosition() - offset;
        long termination = j - samples;
        for (; j >= 0 && j > termination; j--) {
            double cleanAcceleration = mAccelerationBuffer.readOne(j) - mGravityBuffer.readOne(j);
            if (cleanAcceleration > mAccelerationThreshold
                    || cleanAcceleration < -mAccelerationThreshold) {
                integral += Math.abs(cleanAcceleration);
            }
        }

        return integral;
    }

    /**
     * get the underlying acceleration buffer for visualization purposes
     *
     * @return the FloatRingBuffer containing the acceleration data
     */
    public FloatRingBuffer getAccelerationBuffer() {
        return mAccelerationBuffer;
    }

    public void setLastIndex(long i) {
        mLastI[3] = mLastI[2];
        mLastI[2] = mLastI[1];
        mLastI[1] = mLastI[0];
        mLastI[0] = i;
    }

    public long getLastIndex(int offset) {
        return mLastI[offset];
    }
}
