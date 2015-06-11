package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

/**
 * Created by Eike Trumann on 11.06.15.
 * All rights reserved
 */
public class FloatingIntegral {
    private double mValue = 0;
    public final int size;

    public FloatingIntegral(double start, int size){
        mValue = start;
        this.size = size;
    }

    public void add(double d){
        mValue += d;
    }

    public void substract(double d){
        mValue -= d;
    }

    public double getValue(){
        return mValue;
    }

    public int getSize(){
        return size;
    }
}
