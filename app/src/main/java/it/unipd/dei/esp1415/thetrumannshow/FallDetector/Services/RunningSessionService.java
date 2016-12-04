package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.SettingsActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;

/**
 * @author Enrico Naletto
 *         Simple service that tracks the duration of the running session and stops it in case it reaches
 *         the maximum duration set by the user.
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