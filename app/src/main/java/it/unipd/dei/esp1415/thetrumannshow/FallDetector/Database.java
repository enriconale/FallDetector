package it.unipd.dei.esp1415.thetrumannshow.FallDetector;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

/**
 * Created by alessandro on 05/05/15.
 */
public class Database{

    private CreateDatabase helper ;

    public Database(Context ctx){
        helper = new CreateDatabase(ctx);
    }

    public void saveSession(int id, String name, String date, int duration, int color1, int color2, int color3){
        SQLiteDatabase db= helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(CreateDatabase.SESSION_ID, id);
        cv.put(CreateDatabase.SESSION_NAME, name);
        cv.put(CreateDatabase.SESSION_DATE, date);
        cv.put(CreateDatabase.SESSION_DURATION, duration);
        cv.put(CreateDatabase.SESSION_ICON_COLOR_1, color1);
        cv.put(CreateDatabase.SESSION_ICON_COLOR_2, color2);
        cv.put(CreateDatabase.SESSION_ICON_COLOR_3, color3);

        try{
            db.insert(CreateDatabase.SESSION_TABLE, null, cv);
        }
        catch(SQLiteException sqle){

        }
        cv.clear();
    }

    public void saveFall(String name, String date, String location, int xacc, int yacc, int zacc, int sent){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(CreateDatabase.FALL_NAME, name);
        cv.put(CreateDatabase.FALL_DATE, date);
        cv.put(CreateDatabase.FALL_LOCATION, location);
        cv.put(CreateDatabase.X_ACCELERATION, xacc);
        cv.put(CreateDatabase.Y_ACCELERATION, yacc);
        cv.put(CreateDatabase.Z_ACCELERATION, zacc);
        cv.put(CreateDatabase.EMAIL_SENT, sent);

        try{
            db.insert(CreateDatabase.FALL_TABLE, null, cv);
        }
        catch(SQLiteException sqle){

        }
        cv.clear();
    }
}
