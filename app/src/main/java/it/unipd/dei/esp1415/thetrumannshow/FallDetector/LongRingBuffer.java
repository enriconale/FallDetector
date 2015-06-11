package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import java.util.Arrays;

/**
 * Created by Eike Trumann on 31.03.15.
 * all rights reserved
 *
 * The intention of this class is providing an array-like experience for infinite data sources.
 * Data older than the specified capacity will be overwritten thus limiting memory use.
 * This class is highly experimental, do not use it.
 *
 * Performance-wise this class is intended to avoid memory allocation and object creation.
 * Write and read operations should be in O(1).
 */
public class LongRingBuffer{
    private long[] dataStore;
    private final int capacity;
    private int nextPosition;

    public LongRingBuffer(int actualCapacity){
        capacity = actualCapacity;
        dataStore = new long[capacity];
    }

    public void insert(long l){
        dataStore[nextPosition] = l;
        nextPosition = ++nextPosition % capacity;
    }

    public long readOne(long position){
        while(position < 0){
            position += capacity;
        }
        int pos = (int) (position % capacity);
        return dataStore[pos];
    }

    public long getPosition(long data){
        return Arrays.binarySearch(dataStore, data);
    }

    public long[] readRange(long start, long end){
        if (end < start)
            throw new NegativeArraySizeException();

        while(start < 0){
            start += capacity;
            end += capacity;
        }

        //if (start % capacity < end % capacity){
        //    return Arrays.copyOfRange(dataStore, (int) (start % capacity), (int) (end % capacity));
        //}

        long[] ret = new long[(int) (end - start + 1)];
        for (long i = start; i <= end; i++){
            ret[(int) (i - start)] = readOne(i);
        }
        return ret;
    }

    public LongRingBuffer copy() {
        LongRingBuffer ret = new LongRingBuffer(capacity);
        for (long l : dataStore){
            ret.insert(l);
        }
        return ret;
    }

    public long getCurrentPosition(){
        return nextPosition - 1;
    }
}
