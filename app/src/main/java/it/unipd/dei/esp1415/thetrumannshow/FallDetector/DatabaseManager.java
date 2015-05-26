package it.unipd.dei.esp1415.thetrumannshow.FallDetector;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import com.google.android.gms.drive.internal.CreateContentsRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by alessandro on 05/05/15.
 */
public class DatabaseManager {

    private static CreateDatabase mDbHelper;
    private SQLiteDatabase mDatabase;
    private ContentValues mContentValues;
    private Context mAppContext;
    private String mSessionTableAllColumns[] = {
        CreateDatabase.SESSION_ID,
        CreateDatabase.SESSION_NAME,
        CreateDatabase.SESSION_DATE,
        CreateDatabase.SESSION_DURATION,
        CreateDatabase.SESSION_ICON_COLOR_1,
        CreateDatabase.SESSION_ICON_COLOR_2,
        CreateDatabase.SESSION_ICON_COLOR_3,
    };

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

        try {
            mDatabase.insert(CreateDatabase.SESSION_TABLE, null, mContentValues);
        } catch (SQLiteException sqle) {

        }
        mContentValues.clear();
        close();
    }

    public void saveFall(Fall fall) {
        open();
        mContentValues.put(CreateDatabase.FALL_NAME, fall.getName());
        mContentValues.put(CreateDatabase.FALL_DATE, SessionsLab.get(mAppContext).getDateFormat().format(fall
                .getDate()));
        mContentValues.put(CreateDatabase.FALL_LATITUDE, fall.getLatitude());
        mContentValues.put(CreateDatabase.FALL_LONGITUDE, fall.getLongitude());
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
                mSessionTableAllColumns, null, null, null, null, null);

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

    public void deleteSession(Session session) {
        open();

        String[] sessionId = new String[] {session.getUUID().toString()};

        try {
            mDatabase.delete(CreateDatabase.SESSION_TABLE, CreateDatabase.SESSION_ID + " LIKE ?",
                    sessionId);
        } catch (SQLiteException e) {
            Toast.makeText(mAppContext, "An error occurred while deleting the session", Toast
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

        return new Session(sessionUUID, sessionName, sessionDate, sessionDuration,
                sessionIconColor1, sessionIconColor2, sessionIconColor3);
    }

    private String formatFloatArray(float[] data) {
        StringBuilder builder = new StringBuilder(500);
        for (int i = 0; i < data.length - 1; i++) {
            builder.append(data[i]);
            builder.append("-");
        }
        builder.append(data[data.length - 1]);
        return builder.toString();
    }

    private synchronized void open() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    private synchronized void close() {
        mDatabase.close();
    }
}
