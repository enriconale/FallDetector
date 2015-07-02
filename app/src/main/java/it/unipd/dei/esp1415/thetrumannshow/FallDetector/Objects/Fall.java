package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects;

import android.location.Address;
import android.location.Location;

import java.util.Date;

/**
 * @author Enrico Naletto
 */
public class Fall {
    private final String mFallName;
    private final Date mDate;

    private Double mLatitude;
    private Double mLongitude;

    private Address mAddress;

    private final float[] mXAcceleration;
    private final float[] mYAcceleration;
    private final float[] mZAcceleration;
    private boolean mIsEmailSent;

    public Fall(String name, Date date, Double latitude, Double longitude, float[] xAcc, float[]
            yAcc, float[] zAcc) {
        mFallName = name;
        mDate = date;
        mLatitude = latitude;
        mLongitude = longitude;
        mXAcceleration = xAcc;
        mYAcceleration = yAcc;
        mZAcceleration = zAcc;
    }

    public String getName() {
        return mFallName;
    }

    public Date getDate() {
        return mDate;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public float[] getXAcceleration() {
        return mXAcceleration;
    }

    public float[] getYAcceleration() {
        return mYAcceleration;
    }

    public float[] getZAcceleration() {
        return mZAcceleration;
    }

    public void setIsEmailSent(boolean isEmailSent) {
        mIsEmailSent = isEmailSent;
    }

    public void setLocation(Location loc){
        mLatitude = loc.getLatitude();
        mLongitude = loc.getLongitude();
    }

    public boolean isEmailSent() {
        return mIsEmailSent;
    }
}
