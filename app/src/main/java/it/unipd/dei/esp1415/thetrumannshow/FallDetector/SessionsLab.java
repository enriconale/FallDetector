package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author Enrico Naletto
 * Singleton class to manage the list of sessions and the currently active session.
 */
public class SessionsLab {
    private static boolean mHasRunningSession = false;
    private static boolean mIsRunningSessionPlaying = false;
    private static boolean mIsRunningSessionAlreadySavedInDatabase = false;
    private static SimpleDateFormat mDateFormatter;
    private static SessionsLab sSessionsLab;
    private static DatabaseManager mDatabaseManager;
    private Context mAppContext;
    private ArrayList<Session> mSessionsList;
    private Session mRunningSession;
    private DataAcquisitionUnit mDataAcquisitionUnit;

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
        mDataAcquisitionUnit = new DataAcquisitionUnit(mAppContext);
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
        mDataAcquisitionUnit.detach();
    }

    public boolean hasRunningSession() {
        return mHasRunningSession;
    }

    public boolean isRunningSessionPlaying() {
        return mIsRunningSessionPlaying;
    }

    public LinkedList<Fall> getFallsOfSession(Session session) {
        return mDatabaseManager.getFallsFromDatabase(session);
    }

    public void saveRunningSessionInDatabase() {
        if (mIsRunningSessionAlreadySavedInDatabase) {
            mDatabaseManager.updateRunningSessionInDatabase();
        } else {
            mDatabaseManager.saveSession(mRunningSession);
            mIsRunningSessionAlreadySavedInDatabase = true;
        }

    }

    public void saveFallInDatabase(Fall fall) {
        mDatabaseManager.saveFall(fall);
    }

    public void deleteSessionFromDatabase(Session session) {
        mDatabaseManager.deleteSession(session);
    }

}
