package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.RunningSessionActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.SettingsActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Database.DatabaseManager;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;

/**
 * @author Enrico Naletto
 *         Singleton class to manage the list of sessions, the currently running session, the
 *         saving to (and loading from) the database.
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

    //Private constructor
    private SessionsLab(Context appContext) {
        mAppContext = appContext;
        mDatabaseManager = new DatabaseManager(appContext);
        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault());
        mSessionsList = mDatabaseManager.getAllSessionsFromDatabase();
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

    public SimpleDateFormat getDateFormat() {
        return mDateFormatter;
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

    public LinkedList<Fall> getFallsOfSession(Session session) {
        return mDatabaseManager.getFallsFromDatabase(session);
    }

    //Saves (or updates in case it has been already saved) the running session into the database
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
