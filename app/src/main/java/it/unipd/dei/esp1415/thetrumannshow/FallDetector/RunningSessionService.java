package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Enrico Naletto
 */
public class RunningSessionService extends IntentService {
    private final long ONE_HOUR_IN_MILLIS = 3600000;
    private Timer mTimer;
    private long mExecutionTime = 0;
    private SessionsLab mSessionsLab;
    long mMaxSessionDuration;

    public RunningSessionService() {
        super("RunningSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext());
        mMaxSessionDuration = Integer.parseInt(sharedPrefs.getString(SettingsActivity
                        .PREF_SESSION_DURATION, "12")) * ONE_HOUR_IN_MILLIS;

        mSessionsLab = SessionsLab.get(getApplicationContext());
        final Session mRunningSession = mSessionsLab.getRunningSession();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mSessionsLab.hasRunningSession()) {
                    if (mSessionsLab.isRunningSessionPlaying()) {
                        mExecutionTime += 1000;
                        mRunningSession.setDuration(mExecutionTime);
                        if (mExecutionTime >= mMaxSessionDuration) {
                            mSessionsLab.stopCurrentlyRunningSession();
                            mTimer.cancel();
                        }
                    }
                } else {
                    mTimer.cancel();
                }
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy() {

    }
}