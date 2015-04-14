package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

/**
 * @author Enrico Naletto
 */
public class RunningSessionService extends IntentService {

    private Session mRunningSession;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MainActivity.STOP_RUNNING_SERVICE)){
                stopSelf();
            }
        }
    };

    public RunningSessionService() {
        super("RunningSessionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("Service started", "Service started");
        mRunningSession = SessionsLab.get(getApplicationContext()).getRunningSession();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.STOP_RUNNING_SERVICE);
        registerReceiver(receiver, filter);

        while (true) {
            Log.d("Service going on", mRunningSession.getSessionName());
            SystemClock.sleep(1000);
        }

    }

    @Override
    public void onDestroy() {
        Log.d("destroyed", "destroyed");
        unregisterReceiver(receiver);
    }
}
