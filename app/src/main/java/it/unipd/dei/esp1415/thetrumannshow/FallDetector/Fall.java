package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.location.Location;

import java.util.Date;

/**
 * @author Enrico Naletto
 */
public class Fall {
    private final String mFallName;
    private final Date mDate;
    private Location mLocation;
    private final float[] mXAcceleration;
    private final float[] mYAcceleration;
    private final float[] mZAcceleration;
    private boolean mIsEmailSent;

    public Fall(String name, Date date, Location location, float[] xAcc, float[] yAcc, float[] zAcc) {
        mFallName = name;
        mDate = date;
        mLocation = location;
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

    public Location getLocation() {
        return mLocation;
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

    public void setLocation(Location loc){ mLocation = loc; };
}
