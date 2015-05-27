package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
    private static int mFallNameIndex = 0;

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
        mFallNameIndex++;
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


        new DelayedLocationProvider(mLastFall, mGoogleApiClient, this, mContext);
    }

    public static void resetFallNameCounter() {
        mFallNameIndex = 0;
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
        return new Fall("Fall #" + mFallNameIndex ,new java.util.Date(), null, null ,xArr,yArr,
                zArr);
    }

    void locationFixed(){
        new DelayedReverseGeocoder(mLastFall, mGoogleApiClient, mContext);

        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("softwaretest@eiketrumann.de") +
                "?subject=" + Uri.encode("Sono Caduto") +
                "&body=" + Uri.encode("Vieni a prendermi a www.google.com/maps/preview/@"+ mLastFall.getLatitude() + "," + mLastFall.getLongitude()+",8z");
        Uri uri = Uri.parse(uriText);
        send.setData(uri);
        send.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(send);
    }
}
