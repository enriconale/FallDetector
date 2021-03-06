package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.IconColor;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.CurrentLocale;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;

/**
 * @author Alessandro Fuser
 */
public class SessionDbManager {

    private AppDatabaseHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private ContentValues mContentValues;
    private Context mAppContext;
    private SimpleDateFormat mDateFormatter;


    public SessionDbManager(Context ctx) {
        mAppContext = ctx;
        mContentValues = new ContentValues();
        mDbHelper = new AppDatabaseHelper(ctx);
        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                CurrentLocale.getCurrentLocale(mAppContext));
    }

    public void saveSession(Session session) {
        openDatabaseConnection();
        mContentValues.put(AppDatabaseHelper.SESSION_ID, session.getUUID().toString());
        mContentValues.put(AppDatabaseHelper.SESSION_NAME, session.getSessionName());
        mContentValues.put(AppDatabaseHelper.SESSION_DATE, mDateFormatter.format(session.getDate()));
        mContentValues.put(AppDatabaseHelper.SESSION_DURATION, session.getDuration());
        mContentValues.put(AppDatabaseHelper.SESSION_ICON_COLOR, session.getIconColorRgbValue());
        mContentValues.put(AppDatabaseHelper.SESSION_NUMBER_OF_FALLS, session.getNumberOfFalls());

        try {
            mDatabase.insert(AppDatabaseHelper.SESSION_TABLE, null, mContentValues);
        } catch (SQLiteException sqle) {
            Toast.makeText(mAppContext, R.string.database_exception, Toast.LENGTH_SHORT).show();
        }
        mContentValues.clear();
        closeDatabaseConnection();
    }

    public ArrayList<Session> getAllSessionsFromDatabase() {
        openDatabaseConnection();
        ArrayList<Session> sessions = new ArrayList<>();

        Cursor cursor = mDatabase.query(AppDatabaseHelper.SESSION_TABLE,
                null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Session session = getSessionFromCursor(cursor);
            sessions.add(0, session);
            cursor.moveToNext();
        }
        // make sure to closeDatabaseConnection the cursor
        cursor.close();
        closeDatabaseConnection();
        return sessions;
    }

    public void deleteSession(Session session) {
        openDatabaseConnection();

        String[] sessionId = new String[]{session.getUUID().toString()};

        try {
            mDatabase.delete(AppDatabaseHelper.SESSION_TABLE, AppDatabaseHelper.SESSION_ID + " LIKE ?",
                    sessionId);
        } catch (SQLiteException e) {
            Toast.makeText(mAppContext, R.string.error_deleting_session, Toast
                    .LENGTH_SHORT).show();
        } finally {
            closeDatabaseConnection();
        }
    }

    private Session getSessionFromCursor(Cursor cursor) {
        UUID sessionUUID = UUID.fromString(cursor.getString(cursor.getColumnIndex(AppDatabaseHelper
                .SESSION_ID)));

        String sessionName = cursor.getString(cursor.getColumnIndex(AppDatabaseHelper.SESSION_NAME));

        Date sessionDate;
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                    CurrentLocale.getCurrentLocale(mAppContext));
            sessionDate = dateFormatter.parse(cursor.getString
                    (cursor.getColumnIndex(AppDatabaseHelper.SESSION_DATE)));
        } catch (ParseException e) {
            sessionDate = new Date();
        }

        long sessionDuration = cursor.getLong(cursor.getColumnIndex(AppDatabaseHelper
                .SESSION_DURATION));

        int sessionIconColor1 = cursor.getInt(cursor.getColumnIndex(AppDatabaseHelper
                .SESSION_ICON_COLOR));

        int sessionNumberOfFalls = cursor.getInt(cursor.getColumnIndex(AppDatabaseHelper
                .SESSION_NUMBER_OF_FALLS));

        return new Session.Builder()
                .UUID(sessionUUID)
                .sessionName(sessionName)
                .startDate(sessionDate)
                .duration(sessionDuration)
                .iconColor(sessionIconColor1)
                .numberOfFalls(sessionNumberOfFalls)
                .build();
    }

    public void updateRunningSessionInDatabase() {
        Session runningSession = SessionsLab.get(mAppContext).getRunningSession();
        String[] idString = new String[]{runningSession.getUUID().toString()};

        openDatabaseConnection();
        mContentValues.put(AppDatabaseHelper.SESSION_NAME, runningSession.getSessionName());
        mContentValues.put(AppDatabaseHelper.SESSION_DURATION, runningSession.getDuration());
        mContentValues.put(AppDatabaseHelper.SESSION_NUMBER_OF_FALLS, runningSession
                .getNumberOfFalls());

        // Save Values into database
        try {
            mDatabase.update(AppDatabaseHelper.SESSION_TABLE, mContentValues, AppDatabaseHelper.SESSION_ID +
                    " LIKE ?", idString);
        } catch (SQLiteException sqle) {
            Toast.makeText(mAppContext, R.string.error_updating_database,
                    Toast.LENGTH_SHORT).show();
        } finally {
            mContentValues.clear();
            closeDatabaseConnection();
        }
    }

    private synchronized void openDatabaseConnection() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    private synchronized void closeDatabaseConnection() {
        mDatabase.close();
    }
}
