package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Session;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.CurrentLocale;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;

/**
 * @author Alessandro Fuser
 */
public class FallDbManager {

    private AppDatabaseHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private ContentValues mContentValues;
    private Context mAppContext;
    private SimpleDateFormat mDateFormatter;


    public FallDbManager(Context ctx) {
        mAppContext = ctx;
        mContentValues = new ContentValues();
        mDbHelper = new AppDatabaseHelper(ctx);
        mDateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                CurrentLocale.getCurrentLocale(mAppContext));
    }

    public void saveFall(Fall fall) {
        SessionsLab.get(mAppContext).saveRunningSessionInDatabase();
        openDatabaseConnection();
        mContentValues.put(AppDatabaseHelper.FALL_NAME, fall.getName());
        mContentValues.put(AppDatabaseHelper.FALL_DATE, mDateFormatter.format(fall
                .getDate()));

        if (fall.getLatitude() != null) {
            mContentValues.put(AppDatabaseHelper.FALL_LATITUDE, fall.getLatitude());
        } else {
            mContentValues.put(AppDatabaseHelper.FALL_LATITUDE, 0);
        }

        if (fall.getLongitude() != null) {
            mContentValues.put(AppDatabaseHelper.FALL_LONGITUDE, fall.getLongitude());
        } else {
            mContentValues.put(AppDatabaseHelper.FALL_LONGITUDE, 0);
        }

        mContentValues.put(AppDatabaseHelper.X_ACCELERATION, formatFloatArray(fall.getXAcceleration()));
        mContentValues.put(AppDatabaseHelper.Y_ACCELERATION, formatFloatArray(fall.getYAcceleration()));
        mContentValues.put(AppDatabaseHelper.Z_ACCELERATION, formatFloatArray(fall.getZAcceleration()));
        mContentValues.put(AppDatabaseHelper.OWNER_SESSION, SessionsLab.get(mAppContext).getRunningSession()
                .getUUID().toString());

        try {
            mDatabase.insert(AppDatabaseHelper.FALL_TABLE, null, mContentValues);
        } catch (SQLiteException sqle) {
            Toast.makeText(mAppContext, R.string.database_exception, Toast.LENGTH_SHORT).show();
        } finally {
            mContentValues.clear();
            closeDatabaseConnection();
        }
    }

    public LinkedList<Fall> getFallsFromDatabase(Session session) {
        openDatabaseConnection();
        UUID sessionUUID = session.getUUID();
        LinkedList<Fall> fallsList = new LinkedList<>();

        Cursor cursor = mDatabase.query(AppDatabaseHelper.FALL_TABLE, null, AppDatabaseHelper
                        .OWNER_SESSION + " = '" + sessionUUID.toString() + "' ",
                null, null, null, "ROWID");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Fall fall = getFallFromCursor(cursor);
            fallsList.add(fall);
            cursor.moveToNext();
        }

        cursor.close();
        closeDatabaseConnection();
        return fallsList;
    }

    private Fall getFallFromCursor(Cursor cursor) {
        String fallName = cursor.getString(cursor.getColumnIndex(AppDatabaseHelper.FALL_NAME));

        Date fallDate;
        try {
            fallDate = mDateFormatter.parse(cursor.getString
                    (cursor.getColumnIndex(AppDatabaseHelper.FALL_DATE)));
        } catch (ParseException e) {
            fallDate = new Date();
        }

        Double fallLatitude = cursor.getDouble(cursor.getColumnIndex(AppDatabaseHelper.FALL_LATITUDE));
        Double fallLongitude = cursor.getDouble(cursor.getColumnIndex(AppDatabaseHelper
                .FALL_LONGITUDE));

        float[] xAccelerationData = parseAccelerationDataFromString(cursor.getString(cursor
                .getColumnIndex(AppDatabaseHelper.X_ACCELERATION)));

        float[] yAccelerationData = parseAccelerationDataFromString(cursor.getString(cursor
                .getColumnIndex(AppDatabaseHelper.Y_ACCELERATION)));

        float[] zAccelerationData = parseAccelerationDataFromString(cursor.getString(cursor
                .getColumnIndex(AppDatabaseHelper.Z_ACCELERATION)));

        Fall resultFall;
        if (fallLatitude == 0) {
            fallLatitude = null;
            fallLongitude = null;
        }

        resultFall = new Fall.Builder()
                .fallName(fallName)
                .date(fallDate)
                .latitude(fallLatitude)
                .longitude(fallLongitude)
                .xAcceleration(xAccelerationData)
                .yAcceleration(yAccelerationData)
                .zAcceleration(zAccelerationData)
                .build();

        return resultFall;
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

    private synchronized void openDatabaseConnection() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
    }

    private synchronized void closeDatabaseConnection() {
        mDatabase.close();
    }
}
