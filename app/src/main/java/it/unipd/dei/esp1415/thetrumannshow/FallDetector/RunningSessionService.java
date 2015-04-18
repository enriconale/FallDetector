package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Enrico Naletto
 */
public class RunningSessionService extends IntentService {
    private Timer mTimer;
    private long mExecutionTime;

    public RunningSessionService() {
        super("RunningSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service started", "Service started");
        final Session mRunningSession = SessionsLab.get(getApplicationContext()).getRunningSession();


        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (SessionsLab.get(getApplicationContext()).hasRunningSession()) {
                    Log.d("Service going on", mRunningSession.getSessionName());
                    mExecutionTime += 1000;
                    mRunningSession.setDuration(mExecutionTime);
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