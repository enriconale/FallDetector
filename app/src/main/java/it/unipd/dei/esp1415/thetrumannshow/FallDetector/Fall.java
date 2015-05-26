package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;

import java.util.Date;

/**
 * @author Enrico Naletto
 */
public class Fall extends ActionBarActivity{
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

        /*Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("email@gmail.com") +
                "?subject=" + Uri.encode("Sono Caduto") +
                "&body=" + Uri.encode("Vieni a prendermi a " + mLatitude + mLongitude);
        Uri uri = Uri.parse(uriText);

        send.setData(uri);
        startActivity(Intent.createChooser(send, "Send mail..."));*/
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

    public void setAddress(Address addr){
        mAddress = addr;
    }

    public boolean isEmailSent() {
        return mIsEmailSent;
    }
}
