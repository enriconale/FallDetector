package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

/**
 * Created by Eike Trumann on 31.03.15.
 * all rights reserved
 *
 * The intention of this class is providing an array-like experience for infinite data sources.
 * Data older than the specified capacity will be overwritten thus limiting memory use.
 *
 * The implementation is a circular first-in-first-deleted ring buffer.
 *
 * Performance-wise this class is intended to avoid memory allocation and object creation.
 * Write and read operations should be in O(1).
 *
 * There is an infinite mapping between the limited space and the indexes based on modulo operation.
 */
public class FloatRingBuffer{
    private float[] dataStore;
    private final int capacity;
    private int nextPosition;

    public FloatRingBuffer(int actualCapacity){
        capacity = actualCapacity;
        dataStore = new float[capacity];
    }

    /**
     * insert a value at the next position and count up the posion counter
     * @param f number to insert
     */
    public void insert(float f){
        dataStore[nextPosition] = f;
        // in case of reaching the end of the actual capacity start from the beginning
        nextPosition = ++nextPosition % capacity;
    }

    /**
     * get the number stored at the given position
     * @param position index to be read (may be offset by n*capacity)
     * @return the stored value
     */
    public float readOne(long position){
        while(position < 0){
            position += capacity;
        }
        int pos = (int) (position % capacity);
        return dataStore[pos];
    }

    /**
     * get a copy of a part of the buffer
     * @param start index of the first element to be copied
     * @param end index of the last element to be copied
     * @return a copy of the requested interval
     */
    public float[] readRange(long start, long end){
        if (end < start)
            throw new NegativeArraySizeException();

        while(start < 0){
            start += capacity;
            end += capacity;
        }

        float[] ret = new float[(int) (end - start + 1)];
        for (long i = start; i <= end; i++){
            ret[(int) (i - start)] = readOne(i);
        }
        return ret;
    }

    /**
     * get a copy of this buffer
     * the copy should not be used for write purposes
     * @return a copy of the buffer
     */
    public FloatRingBuffer copy() {
        FloatRingBuffer ret = new FloatRingBuffer(capacity);
        for (float f : dataStore){
            ret.insert(f);
        }
        return ret;
    }

    /**
     * get the position where the last value was inserted
     * @return the last insertion position
     */
    public long getCurrentPosition(){
        return nextPosition - 1;
    }
}