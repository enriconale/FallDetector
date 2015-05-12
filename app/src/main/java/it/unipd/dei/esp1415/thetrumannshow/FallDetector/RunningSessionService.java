package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.IntentService;
import android.content.Intent;
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

    public RunningSessionService() {
        super("RunningSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service started", "Service started");
        mSessionsLab = SessionsLab.get(getApplicationContext());
        final Session mRunningSession = mSessionsLab.getRunningSession();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mSessionsLab.hasRunningSession()) {
                    Log.d("Service going on", mRunningSession.getSessionName());
                    if (mSessionsLab.isRunningSessionPlaying()) {
                        mExecutionTime += 1000;
                        mRunningSession.setDuration(mExecutionTime);
                    }
                } else {
                    mTimer.cancel();
                }
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy() {
        Log.d("destroyed", "destroyed");
    }
}