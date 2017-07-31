package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Database.SessionDbManager;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;

/**
 * @author Enrico Naletto
 *         Singleton class to manage the list of sessions, the currently running session, the
 *         saving to (and loading from) the database.
 */
public class SessionsLab {
    private static boolean mHasRunningSession = false;
    private static boolean mIsRunningSessionPlaying = false;
    private static boolean mIsRunningSessionAlreadySavedInDatabase = false;
    private static SessionsLab sSessionsLab;
    private SessionDbManager mSessionDbManager;
    private Context mAppContext;
    private ArrayList<Session> mSessionsList;
    private Session mRunningSession;
    private DataAcquisitionUnit mDataAcquisitionUnit;

    //Private constructor
    private SessionsLab(Context appContext) {
        mAppContext = appContext;
        mSessionDbManager = new SessionDbManager(appContext);
        mSessionsList = mSessionDbManager.getAllSessionsFromDatabase();
    }

    //Public method used to get the unique instance of SessionsLab.
    public static SessionsLab get(Context c) {
        if (sSessionsLab == null) {
            sSessionsLab = new SessionsLab(c.getApplicationContext());
        }
        return sSessionsLab;
    }

    //Creates and starts a new session
    public void createNewRunningSession(Session session) {
        mRunningSession = session;
        mHasRunningSession = true;
        mIsRunningSessionPlaying = true;
        mDataAcquisitionUnit = new DataAcquisitionUnit(mAppContext);
        mIsRunningSessionAlreadySavedInDatabase = false;
    }

    public ArrayList<Session> getSessions() {
        return mSessionsList;
    }

    public Session getRunningSession() {
        return mRunningSession;
    }

    //Resumes the running session when paused, and accordingly changes the ongoing notification (if
    // present)
    public void resumeCurrentlyRunningSession() {
        mIsRunningSessionPlaying = true;
        mDataAcquisitionUnit.resume();
        PersistentNotificationManager.createPersistentNotificationForRunningSession(mAppContext);
    }

    //Pauses the running session, and accordingly changes the ongoing notification (if present)
    public void pauseCurrentlyRunningSession() {
        mIsRunningSessionPlaying = false;
        mDataAcquisitionUnit.detach();
        saveRunningSessionInDatabase();
        PersistentNotificationManager.createPersistentNotificationForPausedSession(mAppContext);
    }

    //Stops and saves into the database the running session
    public void stopCurrentlyRunningSession() {
        saveRunningSessionInDatabase();
        mIsRunningSessionPlaying = false;
        mHasRunningSession = false;
        mDataAcquisitionUnit.detach();
        mDataAcquisitionUnit = null;
        PersistentNotificationManager.deletePersistentNotification(mAppContext);
        mRunningSession = null;
    }

    public boolean hasRunningSession() {
        return mHasRunningSession;
    }

    public boolean isRunningSessionPlaying() {
        return mIsRunningSessionPlaying;
    }

    //Saves (or updates in case it has been already saved) the running session into the database
    public void saveRunningSessionInDatabase() {
        if (mIsRunningSessionAlreadySavedInDatabase) {
            mSessionDbManager.updateRunningSessionInDatabase();
        } else {
            mSessionDbManager.saveSession(mRunningSession);
            mIsRunningSessionAlreadySavedInDatabase = true;
        }

    }
}
