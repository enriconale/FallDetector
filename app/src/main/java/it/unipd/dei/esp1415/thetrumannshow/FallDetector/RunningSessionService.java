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

    private Session mRunningSession;
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind;

    public RunningSessionService() {
        super("RunningSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service started", "Service started");
        mRunningSession = SessionsLab.get(getApplicationContext()).getRunningSession();

        while (true) {
            if (SessionsLab.get(getApplicationContext()).hasRunningSession())
                Log.d("Service going on", mRunningSession.getSessionName());
            else
                break;
            SystemClock.sleep(1000);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        stopSelf();
        return mAllowRebind;
    }

    @Override
    public void onDestroy() {
        Log.d("destroyed", "destroyed");
    }
}
