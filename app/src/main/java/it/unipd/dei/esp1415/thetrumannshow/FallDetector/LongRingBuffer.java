package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

/**
 * Created by Eike Trumann on 31.03.15.
 * all rights reserved
 */
public class LongRingBuffer{
    private long[] dataStore;
    private final int capacity;
    private int nextPosition;

    public LongRingBuffer(int actualCapacity){
        capacity = actualCapacity;
    }

    public void insert(long l){
        dataStore[nextPosition] = l;
        nextPosition = ++nextPosition % capacity;
    }
}
