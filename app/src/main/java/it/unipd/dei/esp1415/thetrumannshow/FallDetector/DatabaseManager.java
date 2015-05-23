package it.unipd.dei.esp1415.thetrumannshow.FallDetector;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * Created by alessandro on 05/05/15.
 */
public class DatabaseManager {

    private CreateDatabase helper;
    private Context mAppContext;

    public DatabaseManager(Context ctx){
        mAppContext = ctx;
        helper = new CreateDatabase(ctx);
    }

    public void saveSession(Session session) {

        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(CreateDatabase.SESSION_ID, session.getUUID().toString());
        cv.put(CreateDatabase.SESSION_NAME, session.getSessionName());
        cv.put(CreateDatabase.SESSION_DATE, SessionsLab.get(mAppContext).getDateFormat().format
                (session.getDate()));
        cv.put(CreateDatabase.SESSION_DURATION, session.getDuration());
        cv.put(CreateDatabase.SESSION_ICON_COLOR_1, session.getColor1());
        cv.put(CreateDatabase.SESSION_ICON_COLOR_2, session.getColor2());
        cv.put(CreateDatabase.SESSION_ICON_COLOR_3, session.getColor3());

        try {
            db.insert(CreateDatabase.SESSION_TABLE, null, cv);
        } catch (SQLiteException sqle) {

        }

        cv.clear();
    }

    public void saveFall(Fall fall) {
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(CreateDatabase.FALL_NAME, fall.getName());
        cv.put(CreateDatabase.FALL_DATE, SessionsLab.get(mAppContext).getDateFormat().format(fall
                .getDate()));
        cv.put(CreateDatabase.FALL_LATITUDE, fall.getLatitude());
        cv.put(CreateDatabase.FALL_LONGITUDE, fall.getLongitude());
        cv.put(CreateDatabase.X_ACCELERATION, formatFloatArray(fall.getXAcceleration()));
        cv.put(CreateDatabase.Y_ACCELERATION, formatFloatArray(fall.getYAcceleration()));
        cv.put(CreateDatabase.Z_ACCELERATION, formatFloatArray(fall.getZAcceleration()));

        if (fall.isEmailSent()) {
            cv.put(CreateDatabase.EMAIL_SENT, 1);
        } else {
            cv.put(CreateDatabase.EMAIL_SENT, 0);
        }

        cv.put(CreateDatabase.OWNER_SESSION, SessionsLab.get(mAppContext).getRunningSession()
                .getUUID().toString());


        try {
            db.insert(CreateDatabase.FALL_TABLE, null, cv);
        } catch (SQLiteException sqle) {

        }

        cv.clear();
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
}
