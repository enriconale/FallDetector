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
 * Singleton class to manage the list of sessions, the currently running session, the
 * saving to (and loading from) the database.
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
    NotificationManager mNotificationManager;

    //Private constructor
    private SessionsLab(Context appContext) {
        mAppContext = appContext;
        mDatabaseManager = new DatabaseManager(appContext);
        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault());
        mSessionsList = mDatabaseManager.getAllSessionsFromDatabase();
        mNotificationManager = (NotificationManager) mAppContext.getSystemService(Context
                .NOTIFICATION_SERVICE);
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

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    //Resumes the running session when paused, and accordingly changes the ongoing notification (if
    // present)
    public void resumeCurrentlyRunningSession() {
        mIsRunningSessionPlaying = true;
        mDataAcquisitionUnit.resume();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        boolean userWantsOnGoingNotification = sp.getBoolean(SettingsActivity
                .PREF_ONGOING_NOTIFICATION, true);
        if (userWantsOnGoingNotification) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mAppContext)
                            .setSmallIcon(R.mipmap.notification_icon)
                            .setContentTitle(mAppContext.getString(R.string.notification_title_text))
                            .setContentText(mAppContext.getString(R.string
                                    .notification_content_text))
                            .setOngoing(true);

            Intent resultIntent = new Intent(mAppContext, RunningSessionActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mAppContext);
            stackBuilder.addParentStack(RunningSessionActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    //Pauses the running session, and accordingly changes the ongoing notification (if present)
    public void pauseCurrentlyRunningSession() {
        mIsRunningSessionPlaying = false;
        mDataAcquisitionUnit.detach();
        saveRunningSessionInDatabase();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        boolean userWantsOnGoingNotification = sp.getBoolean(SettingsActivity
                .PREF_ONGOING_NOTIFICATION, true);
        if (userWantsOnGoingNotification) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(mAppContext)
                            .setSmallIcon(R.mipmap.notification_icon)
                            .setContentTitle(mAppContext.getString(R.string.notification_title_text_paused))
                            .setContentText(mAppContext.getString(R.string
                                    .notification_content_text_paused))
                            .setOngoing(true);

            Intent resultIntent = new Intent(mAppContext, RunningSessionActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mAppContext);
            stackBuilder.addParentStack(RunningSessionActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    //Stops and saves into the database the running session
    public void stopCurrentlyRunningSession() {
        saveRunningSessionInDatabase();
        mIsRunningSessionPlaying = false;
        mHasRunningSession = false;
        mDataAcquisitionUnit.detach();
        mDataAcquisitionUnit = null;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mAppContext);
        boolean userWantsOnGoingNotification = sp.getBoolean(SettingsActivity
                .PREF_ONGOING_NOTIFICATION, true);
        if (userWantsOnGoingNotification) {
            mNotificationManager.cancel(1);
        }
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