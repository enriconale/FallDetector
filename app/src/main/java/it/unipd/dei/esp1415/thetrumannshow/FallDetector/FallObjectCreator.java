package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;

/**
 * Created by eike on 12.05.15.
 */
public class FallObjectCreator implements Runnable{

    private final GoogleApiClient mGoogleApiClient;
    private final Context mContext;

    private LongRingBuffer timeBuffer;
    private FloatRingBuffer xBuffer;
    private FloatRingBuffer yBuffer;
    private FloatRingBuffer zBuffer;

    private Fall mLastFall;

    private final int mLastFallIndex;

    public FallObjectCreator(LongRingBuffer timeBuffer, FloatRingBuffer xBuffer,
                             FloatRingBuffer yBuffer, FloatRingBuffer zBuffer,
                             Context context, GoogleApiClient googleApiClient, int fallIndex){
        this.timeBuffer = timeBuffer;
        this.xBuffer = xBuffer;
        this.yBuffer = yBuffer;
        this.zBuffer = zBuffer;
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
        this.mLastFallIndex = fallIndex;
    }

    public void run(){
        try{Thread.sleep(800);} catch (Exception e) {
            throw new Error("Who the fuck waked me up? "+e);
        }

        timeBuffer = timeBuffer.copy();
        xBuffer = xBuffer.copy();
        yBuffer = yBuffer.copy();
        zBuffer = zBuffer.copy();

        mLastFall = constructFallObject(mLastFallIndex);
        SessionsLab lab = SessionsLab.get(mContext);
        lab.getRunningSession().addFall(mLastFall);
        lab.saveFallInDatabase(mLastFall);
    }

    long[] getSurroundingSecond(long index){
        long nanosecond = timeBuffer.readOne(index);
        long j = index;
        for(; timeBuffer.readOne(j) > (nanosecond - 500000000); j--){
            if (j < (index - 100)) {
                throw new IndexOutOfBoundsException();
            }
        }
        long begin = j;

        j = index;
        for(; timeBuffer.readOne(j) < (nanosecond + 500000000); j++){
            if (j > (index + 100)) {
                throw new IndexOutOfBoundsException();
            }
        }
        long end = j;
        return new long[]{begin,end};
    }

    private Fall constructFallObject(int index){
        long[] interval = getSurroundingSecond(index);
        float [] xArr = xBuffer.readRange(interval[0],interval[1]);
        float [] yArr = yBuffer.readRange(interval[0],interval[1]);
        float [] zArr = zBuffer.readRange(interval[0],interval[1]);
        return new Fall("",new java.util.Date(), null, null ,xArr,yArr,zArr);
    }

    void locationFixed(){
        new DelayedReverseGeocoder(mLastFall, mGoogleApiClient, mContext);
    }
}