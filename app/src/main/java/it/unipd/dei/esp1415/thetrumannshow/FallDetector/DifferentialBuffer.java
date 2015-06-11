package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved.
 */
public class DifferentialBuffer {
    private final FloatRingBuffer accelerationBuffer;
    private final FloatRingBuffer gravityBuffer;

    private final double intervalMillis;
    private final int samples;

    private double floatingIntegral;

    public DifferentialBuffer(int capacity, int samples, double intervalMillis){
        accelerationBuffer = new FloatRingBuffer(capacity);
        gravityBuffer = new FloatRingBuffer(capacity);
        this.intervalMillis = intervalMillis;
        this.samples = samples;
    }

    public void submitData(float acceleration, float gravity){
        accelerationBuffer.insert(acceleration);
        gravityBuffer.insert(gravity);
        long oldBufferPosition = accelerationBuffer.getCurrentPosition() - samples;
        floatingIntegral += (Math.abs((double)(acceleration-gravity))) * (intervalMillis / 1000);
        floatingIntegral -= (Math.abs((double)(accelerationBuffer.readOne(oldBufferPosition)
                - gravityBuffer.readOne(oldBufferPosition)))) * (intervalMillis / 1000);
    }

    public FloatRingBuffer getAccelerationBuffer(){
        return accelerationBuffer;
    }
}
