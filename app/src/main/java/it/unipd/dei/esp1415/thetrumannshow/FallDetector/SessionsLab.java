package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * @author Enrico Naletto
 * Singleton class to manage the list of sessions and the currently active session.
 */
public class SessionsLab {
    private static boolean mHasRunningSession = false;
    private static boolean mIsRunningSessionPlaying = false;
    private static SimpleDateFormat mDateFormatter;
    private static SessionsLab sSessionsLab;
    private static DatabaseManager mDatabaseManager;
    private Context mAppContext;
    private ArrayList<Session> mSessionsList;
    private Session mRunningSession;

    private SessionsLab(Context appContext) {
        mAppContext = appContext;
        mDatabaseManager = new DatabaseManager(appContext);
        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault()
        );
        mSessionsList = mDatabaseManager.getAllSessionsFromDatabase();
    }

    public static SessionsLab get(Context c) {
        if (sSessionsLab == null) {
            sSessionsLab = new SessionsLab(c.getApplicationContext());
        }
        return sSessionsLab;
    }

    public void createNewRunningSession(Session session) {
        mRunningSession = session;
        mHasRunningSession = true;
        mIsRunningSessionPlaying = true;
    }

    public ArrayList<Session> getSessions() {
        return mSessionsList;
    }

    public Session getRunningSession() {
        return mRunningSession;
    }

    public SimpleDateFormat getDateFormat() {
        return mDateFormatter;
    }

    public void resumeCurrentlyRunningSession() {
        mIsRunningSessionPlaying = true;
    }

    public void pauseCurrentlyRunningSession() {
        mIsRunningSessionPlaying = false;
    }

    public void stopCurrentlyRunningSession() {
        mIsRunningSessionPlaying = false;
        mHasRunningSession = false;
        mRunningSession = null;
    }

    public boolean hasRunningSession() {
        return mHasRunningSession;
    }

    public boolean isRunningSessionPlaying() {
        return mIsRunningSessionPlaying;
    }

    public void saveSessionInDatabase(Session session) {
        mDatabaseManager.saveSession(session);
    }

    public void saveFallInDatabase(Fall fall) {
        mDatabaseManager.saveFall(fall);
    }

    public void deleteSessionFromDatabase(Session session) {
        mDatabaseManager.deleteSession(session);
    }

}
