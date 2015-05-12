package it.unipd.dei.esp1415.thetrumannshow.FallDetector;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLDataException;

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
        cv.put(CreateDatabase.id_session, id);
        cv.put(CreateDatabase.name_session, name);
        cv.put(CreateDatabase.date_session, date);
        cv.put(CreateDatabase.duration_session, duration);
        cv.put(CreateDatabase.color1_icon_session, color1);
        cv.put(CreateDatabase.color2_icon_session, color2);
        cv.put(CreateDatabase.color3_icon_session, color3);

        try{
            db.insert(CreateDatabase.session_table, null, cv);
        }
        catch(SQLiteException sqle){

        }
    }

    public void saveFall(String name, String date, String location, int xacc, int yacc, int zacc, int sent){
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(CreateDatabase.name_fall, name);
        cv.put(CreateDatabase.date_fall, date);
        cv.put(CreateDatabase.location_fall, location);
        cv.put(CreateDatabase.x_acceleration, xacc);
        cv.put(CreateDatabase.y_acceleration, yacc);
        cv.put(CreateDatabase.z_acceleration, zacc);
        cv.put(CreateDatabase.email_sent_fall, sent);

        try{
            db.insert(CreateDatabase.fall_table, null, cv);
        }
        catch(SQLiteException sqle){

        }
    }
}
