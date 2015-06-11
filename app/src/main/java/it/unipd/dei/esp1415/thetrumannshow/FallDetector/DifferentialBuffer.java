package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved.
 */
public class DifferentialBuffer {
    private final FloatRingBuffer accelerationBuffer;
    private final FloatRingBuffer gravityBuffer;

    private final double intervalMicros;
    private final int samples;

    private double floatingIntegral;

    public DifferentialBuffer(int capacity, int samples, double intervalMicros){
        accelerationBuffer = new FloatRingBuffer(capacity);
        gravityBuffer = new FloatRingBuffer(capacity);
        this.intervalMicros = intervalMicros;
        this.samples = samples;
    }

    public void submitData(float acceleration, float gravity){
        accelerationBuffer.insert(acceleration);
        gravityBuffer.insert(gravity);
        long oldBufferPosition = accelerationBuffer.getCurrentPosition() - samples;
        floatingIntegral += (Math.abs((double)(acceleration-gravity))) * (intervalMicros / 1000000);
        floatingIntegral -= (Math.abs((double)(accelerationBuffer.readOne(oldBufferPosition)
                - gravityBuffer.readOne(oldBufferPosition)))) * (intervalMicros / 1000000);
    }

    public FloatRingBuffer getAccelerationBuffer(){
        return accelerationBuffer;
    }

    public float readOne(long position){
        return accelerationBuffer.readOne(position);
    }
}
