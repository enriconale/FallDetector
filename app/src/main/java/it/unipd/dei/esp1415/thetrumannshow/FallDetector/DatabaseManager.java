package it.unipd.dei.esp1415.thetrumannshow.FallDetector;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by alessandro on 05/05/15.
 */
public class DatabaseManager {

    private static CreateDatabase mDbHelper;
    private SQLiteDatabase mDatabase;
    private ContentValues mContentValues;
    private Context mAppContext;


    public DatabaseManager(Context ctx){
        mAppContext = ctx;
        mContentValues = new ContentValues();
        mDbHelper = CreateDatabase.getInstance(ctx);
        //mDatabase = mDbHelper.getWritableDatabase();
    }

    public void saveSession(Session session) {
        open();
        mContentValues.put(CreateDatabase.SESSION_ID, session.getUUID().toString());
        mContentValues.put(CreateDatabase.SESSION_NAME, session.getSessionName());
        mContentValues.put(CreateDatabase.SESSION_DATE, SessionsLab.get(mAppContext).getDateFormat().format
                (session.getDate()));
        mContentValues.put(CreateDatabase.SESSION_DURATION, session.getDuration());
        mContentValues.put(CreateDatabase.SESSION_ICON_COLOR_1, session.getColor1());
        mContentValues.put(CreateDatabase.SESSION_ICON_COLOR_2, session.getColor2());
        mContentValues.put(CreateDatabase.SESSION_ICON_COLOR_3, session.getColor3());
        mContentValues.put(CreateDatabase.SESSION_NUMBER_OF_FALLS, session.getNumberOfFalls());

        try {
            mDatabase.insert(CreateDatabase.SESSION_TABLE, null, mContentValues);
        } catch (SQLiteException sqle) {

        }
        mContentValues.clear();
        close();
    }

    public void saveFall(Fall fall) {
        SessionsLab.get(mAppContext).saveRunningSessionInDatabase();
        open();
        mContentValues.put(CreateDatabase.FALL_NAME, fall.getName());
        mContentValues.put(CreateDatabase.FALL_DATE, SessionsLab.get(mAppContext).getDateFormat().format(fall
                .getDate()));

        if (fall.getLatitude() != null) {
            mContentValues.put(CreateDatabase.FALL_LATITUDE, fall.getLatitude());
        } else {
            mContentValues.put(CreateDatabase.FALL_LATITUDE, 0);
        }

        if (fall.getLongitude() != null) {
            mContentValues.put(CreateDatabase.FALL_LONGITUDE, fall.getLongitude());
        } else {
            mContentValues.put(CreateDatabase.FALL_LONGITUDE, 0);
        }


        mContentValues.put(CreateDatabase.X_ACCELERATION, formatFloatArray(fall.getXAcceleration()));
        mContentValues.put(CreateDatabase.Y_ACCELERATION, formatFloatArray(fall.getYAcceleration()));
        mContentValues.put(CreateDatabase.Z_ACCELERATION, formatFloatArray(fall.getZAcceleration()));

        if (fall.isEmailSent()) {
            mContentValues.put(CreateDatabase.EMAIL_SENT, 1);
        } else {
            mContentValues.put(CreateDatabase.EMAIL_SENT, 0);
        }

        mContentValues.put(CreateDatabase.OWNER_SESSION, SessionsLab.get(mAppContext).getRunningSession()
                .getUUID().toString());


        try {
            mDatabase.insert(CreateDatabase.FALL_TABLE, null, mContentValues);
        } catch (SQLiteException sqle) {

        } finally {
            mContentValues.clear();
            close();
        }
    }

    public ArrayList<Session> getAllSessionsFromDatabase() {
        open();
        ArrayList<Session> sessions = new ArrayList<Session>();

        Cursor cursor = mDatabase.query(CreateDatabase.SESSION_TABLE,
                null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Session session = getSessionFromCursor(cursor);
            sessions.add(0, session);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return sessions;
    }

    public LinkedList<Fall> getFallsFromDatabase(Session session) {
        open();
        UUID sessionUUID = session.getUUID();
        LinkedList<Fall> fallsList = new LinkedList<>();

        Cursor cursor = mDatabase.query(CreateDatabase.FALL_TABLE, null, CreateDatabase
                        .OWNER_SESSION + " = '" + sessionUUID.toString() + "' ",
                null, null, null, "ROWID");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Fall fall = getFallFromCursor(cursor);
            fallsList.add(fall);
            cursor.moveToNext();
        }

        cursor.close();
        close();
        return fallsList;
    }

    public void deleteSession(Session session) {
        open();

        String[] sessionId = new String[] {session.getUUID().toString()};

        try {
            mDatabase.delete(CreateDatabase.SESSION_TABLE, CreateDatabase.SESSION_ID + " LIKE ?",
                    sessionId);
        } catch (SQLiteException e) {
            Toast.makeText(mAppContext, R.string.error_deleting_session , Toast
                    .LENGTH_SHORT).show();
        } finally {
            close();
        }
    }

    private Session getSessionFromCursor(Cursor cursor) {
        UUID sessionUUID = UUID.fromString(cursor.getString(cursor.getColumnIndex(CreateDatabase
                .SESSION_ID)));

        String sessionName = cursor.getString(cursor.getColumnIndex(CreateDatabase.SESSION_NAME));

        Date sessionDate = new Date();
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault());
            sessionDate = dateFormatter.parse(cursor.getString
                    (cursor.getColumnIndex(CreateDatabase.SESSION_DATE)));
        } catch (ParseException e) {
            //TODO manage possible exception
        }

        long sessionDuration = cursor.getLong(cursor.getColumnIndex(CreateDatabase
                .SESSION_DURATION));

        int sessionIconColor1 = cursor.getInt(cursor.getColumnIndex(CreateDatabase
                .SESSION_ICON_COLOR_1));

        int sessionIconColor2 = cursor.getInt(cursor.getColumnIndex(CreateDatabase
                .SESSION_ICON_COLOR_2));

        int sessionIconColor3 = cursor.getInt(cursor.getColumnIndex(CreateDatabase
                .SESSION_ICON_COLOR_3));

        int sessionNumberOfFalls = cursor.getInt(cursor.getColumnIndex(CreateDatabase
                .SESSION_NUMBER_OF_FALLS));

        return new Session(sessionUUID, sessionName, sessionDate, sessionDuration,
                sessionIconColor1, sessionIconColor2, sessionIconColor3, sessionNumberOfFalls);
    }

    private Fall getFallFromCursor(Cursor cursor) {
        String fallName = cursor.getString(cursor.getColumnIndex(CreateDatabase.FALL_NAME));

        Date fallDate = new Date();
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault());
            fallDate = dateFormatter.parse(cursor.getString
                    (cursor.getColumnIndex(CreateDatabase.FALL_DATE)));
        } catch (ParseException e) {
            //TODO manage possible exception
        }

        double fallLatitude = cursor.getDouble(cursor.getColumnIndex(CreateDatabase.FALL_LATITUDE));

        double fallLongitude = cursor.getDouble(cursor.getColumnIndex(CreateDatabase
                .FALL_LONGITUDE));

        float[] xAccelerationData = parseAccelerationDataFromString(cursor.getString(cursor
                .getColumnIndex(CreateDatabase.X_ACCELERATION)));

        float[] yAccelerationData = parseAccelerationDataFromString(cursor.getString(cursor
                .getColumnIndex(CreateDatabase.Y_ACCELERATION)));

        float[] zAccelerationData = parseAccelerationDataFromString(cursor.getString(cursor
                .getColumnIndex(CreateDatabase.Z_ACCELERATION)));

        boolean isEmailSent = false;
        switch (cursor.getInt(cursor.getColumnIndex(CreateDatabase.EMAIL_SENT))) {
            case 0:
                break;
            case 1:
                isEmailSent = true;
                break;
        }

        Fall resultFall;
        if (fallLatitude == 0) {
            resultFall = new Fall(fallName, fallDate, null, null, xAccelerationData,
                    yAccelerationData, zAccelerationData);
        } else {
            resultFall = new Fall(fallName, fallDate, fallLatitude, fallLongitude, xAccelerationData,
                    yAccelerationData, zAccelerationData);
        }

        resultFall.setIsEmailSent(isEmailSent);

        return resultFall;
    }

    public void updateRunningSessionInDatabase() {
        Session runningSession = SessionsLab.get(mAppContext).getRunningSession();
        String[] idString = new String[] {runningSession.getUUID().toString()};

        open();
        mContentValues.put(CreateDatabase.SESSION_NAME, runningSession.getSessionName());
        mContentValues.put(CreateDatabase.SESSION_DURATION, runningSession.getDuration());
        mContentValues.put(CreateDatabase.SESSION_NUMBER_OF_FALLS, runningSession
                .getNumberOfFalls());

        // Save Values into database
        try {
            mDatabase.update(CreateDatabase.SESSION_TABLE, mContentValues, CreateDatabase.SESSION_ID +
                            " LIKE ?", idString );
        } catch (SQLiteException sqle) {
            Toast.makeText(mAppContext, R.string.error_updating_database ,
                    Toast.LENGTH_SHORT).show();
        } finally {
            mContentValues.clear();
            close();
        }
    }

    private String formatFloatArray(float[] data) {
        StringBuilder builder = new StringBuilder(500);
        for (float number : data) {
            builder.append(number);
            builder.append("&");
        }
        return builder.toString();
    }

    private float[] parseAccelerationDataFromString(String data) {
            StringBuilder builder = new StringBuilder(20);
            LinkedList<Float> lst = new LinkedList<>();

            for (int i = 0; i < data.length(); i++) {
                if (Character.toString(data.charAt(i)).equals("&")) {
                    lst.add(Float.parseFloat(builder.toString()));
                    builder.setLength(0);
                } else {
                    builder.append(data.charAt(i));
                }
            }

            float[] result = new float[lst.size()];
            int k = 0;
            for (Float d : lst) {
                result[k++] = d;
            }

            return result;
    }

    private synchronized void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    private synchronized void close() {
        mDatabase.close();
    }
}
