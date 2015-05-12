package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Intent;
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

    /*Intent emailintent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "alessandrofsr@gmail.com", null));
        emailintent.putExtra(emailintent.EXTRA_SUBJECT, "Caduta Tizio");
        emailintent.putExtra(emailintent.EXTRA_TEXT, "Sono caduto, vieni a prendermi a " + mLocation.toString());
        emailintent.setType("message/rfc822");
    startActivity(Intent.createChooser(emailintent, "Send email..."));*/
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
