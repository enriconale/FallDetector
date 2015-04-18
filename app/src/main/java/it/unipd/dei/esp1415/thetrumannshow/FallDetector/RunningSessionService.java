package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * @author Enrico Naletto
 */
public class RunningSessionService extends IntentService {

    public RunningSessionService() {
        super("RunningSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service started", "Service started");
        Session mRunningSession = SessionsLab.get(getApplicationContext()).getRunningSession();

        while (true) {
            if (SessionsLab.get(getApplicationContext()).hasRunningSession())
                Log.d("Service going on", mRunningSession.getSessionName());
            else {
                stopSelf();
                break;
            }
            SystemClock.sleep(1000);
        }

    }

    @Override
    public void onDestroy() {
        Log.d("destroyed", "destroyed");
    }
}
