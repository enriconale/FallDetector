package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.gms.common.api.GoogleApiClient;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.SettingsActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;

/**
 * @author Eike Trumann
 */
public class FallObjectCreator implements Runnable {

    private final GoogleApiClient mGoogleApiClient;
    private final Context mContext;

    private LongRingBuffer timeBuffer;
    private FloatRingBuffer xBuffer;
    private FloatRingBuffer yBuffer;
    private FloatRingBuffer zBuffer;

    private Fall mLastFall;

    private final long mStart;
    private final long mEnd;

    private static int mFallNameIndex = 0;

    public FallObjectCreator(LongRingBuffer timeBuffer, FloatRingBuffer xBuffer,
                             FloatRingBuffer yBuffer, FloatRingBuffer zBuffer,
                             Context context, GoogleApiClient googleApiClient,
                             long start, long end) {
        this.timeBuffer = timeBuffer;
        this.xBuffer = xBuffer;
        this.yBuffer = yBuffer;
        this.zBuffer = zBuffer;
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
        this.mStart = start;
        this.mEnd = end;
        mFallNameIndex++;
    }

    public void run() {
        timeBuffer = timeBuffer.copy();
        xBuffer = xBuffer.copy();
        yBuffer = yBuffer.copy();
        zBuffer = zBuffer.copy();

        mLastFall = constructFallObject();
        mLastFall.setLocation(null);
        SessionsLab lab = SessionsLab.get(mContext);
        lab.getRunningSession().addFall(mLastFall);
        lab.saveFallInDatabase(mLastFall);
//        new DelayedLocationProvider(mLastFall, mGoogleApiClient, this, mContext);
    }

    public static void resetFallNameCounter() {
        mFallNameIndex = 0;
    }

    private Fall constructFallObject() {
        float[] xArr = xBuffer.readRange(mStart, mEnd);
        float[] yArr = yBuffer.readRange(mStart, mEnd);
        float[] zArr = zBuffer.readRange(mStart, mEnd);
        return new Fall("Fall #" + mFallNameIndex, new java.util.Date(), null, null, xArr, yArr,
                zArr);
    }

    void locationFixed() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String email = buildStringOfEmailAddresses(sharedPrefs);

        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(email) +
                "?subject=" + Uri.encode(mContext.getString(R.string.fallen)) +
                "&body=" + Uri.encode(mContext.getString(R.string.google_location) + mLastFall.getLatitude() + "," + mLastFall.getLongitude() + mContext.getString(R.string.resolution));
        Uri uri = Uri.parse(uriText);
        send.setData(uri);
        send.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(send);
        } catch (Exception e) {
            System.err.println("Could not Launch E-Mail-Application: " + e);
        }
    }

    private String buildStringOfEmailAddresses(SharedPreferences sharedPreferences) {
        String emailString1 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS1, "");
        String emailString2 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS2, "");
        String emailString3 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS3, "");
        String emailString4 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS4, "");
        String emailString5 = sharedPreferences.getString(SettingsActivity
                .PREF_EMAIL_ADDRESS5, "");

        String result = "";

        if (!"".equals(emailString1)) {
            result = result + emailString1 + ", ";
        }

        if (!"".equals(emailString2)) {
            result = result + emailString2 + ", ";
        }

        if (!"".equals(emailString3)) {
            result = result + emailString3 + ", ";
        }

        if (!"".equals(emailString4)) {
            result = result + emailString4 + ", ";
        }

        if (!"".equals(emailString5)) {
            result = result + emailString5;
        }

        String x = Character.toString(result.charAt(result.length() - 2));
        if (",".equals(x)) {
            return result.substring(0, result.length() - 2);
        }

        return result;
    }
}
