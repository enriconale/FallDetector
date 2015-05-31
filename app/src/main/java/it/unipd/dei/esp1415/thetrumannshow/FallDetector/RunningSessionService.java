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
    private Timer mTimer;
    private long mExecutionTime = 0;
    private SessionsLab mSessionsLab;
    int mMaxSessionDuration;

    public RunningSessionService() {
        super("RunningSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service started", "Service started");

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences
                (getApplicationContext());
        mMaxSessionDuration = Integer.parseInt(sharedPrefs.getString(SettingsActivity
                        .PREF_SESSION_DURATION, "12"));

        mSessionsLab = SessionsLab.get(getApplicationContext());
        final Session mRunningSession = mSessionsLab.getRunningSession();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mSessionsLab.hasRunningSession()) {
                    Log.d("Service going on", mRunningSession.getSessionName());
                    if (mSessionsLab.isRunningSessionPlaying()) {
                        mExecutionTime += 2000;
                        mRunningSession.setDuration(mExecutionTime);
                        if (convertMillisToHours(mExecutionTime) >= mMaxSessionDuration) {
                            mSessionsLab.stopCurrentlyRunningSession();
                            mTimer.cancel();
                        }
                    }
                } else {
                    mTimer.cancel();
                }
            }
        }, 0, 2000);
    }

    @Override
    public void onDestroy() {
        Log.d("destroyed", "destroyed");
    }

    private int convertMillisToHours(long millis) {
        return (int) ((millis / (1000*60*60)) % 24);
    }
}