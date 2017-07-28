package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by alessandro on 05/05/15.
 * Class that modify the database of the application
 */
public class AppDatabaseHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "Database.db";
    private static final int DB_VERSION = 2;

    //definition of variables for the column of session table
    public static final String SESSION_TABLE = "Session";
    public static final String SESSION_ID = "id";
    public static final String SESSION_NAME = "name";
    public static final String SESSION_DATE = "date";
    public static final String SESSION_DURATION = "duration";
    public static final String SESSION_ICON_COLOR = "icon_color";
    public static final String SESSION_NUMBER_OF_FALLS = "number_of_falls";

    //definition of variables for the column of fall table
    public static final String FALL_TABLE = "Fall";
    public static final String FALL_NAME = "name";
    public static final String FALL_DATE = "date";
    public static final String FALL_LATITUDE = "latitude";
    public static final String FALL_LONGITUDE = "longitude";
    public static final String X_ACCELERATION = "x_acceleration";
    public static final String Y_ACCELERATION = "y_acceleration";
    public static final String Z_ACCELERATION = "z_acceleration";
    public static final String EMAIL_SENT = "email_sent";
    public static final String OWNER_SESSION = "session_id";

    //creation of the session table
    private static final String SESSION_TABLE_CREATE = "create table "
            + SESSION_TABLE + " ("
            + SESSION_ID + " text primary key, "
            + SESSION_NAME + " text, "
            + SESSION_DATE + " text, "
            + SESSION_DURATION + " integer, "
            + SESSION_ICON_COLOR + " integer, "
            + SESSION_NUMBER_OF_FALLS + " integer);";

    //creation of the fall table
    private static final String FALL_TABLE_CREATE = "create table "
            + FALL_TABLE + " ("
            + FALL_NAME + " text, "
            + FALL_DATE + " text, "
            + FALL_LATITUDE + " real, "
            + FALL_LONGITUDE + " real, "
            + X_ACCELERATION + " text, "
            + Y_ACCELERATION + " text, "
            + Z_ACCELERATION + " text, "
            + EMAIL_SENT + " integer, "
            + OWNER_SESSION + " text, "
            + "FOREIGN KEY ("
            + OWNER_SESSION + ") REFERENCES "
            + SESSION_TABLE + "("
            + SESSION_ID + ") ON DELETE CASCADE ON UPDATE CASCADE, "
            + " PRIMARY KEY ("
            + OWNER_SESSION + ", "
            + FALL_NAME + "));";


    private static AppDatabaseHelper mDBHelper;


    public static AppDatabaseHelper getInstance(Context ctx) {

        if (mDBHelper == null) { //this will ensure no multiple instances out there.
            mDBHelper = new AppDatabaseHelper(ctx.getApplicationContext());
        }
        return mDBHelper;
    }

    //constructor
    private AppDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //override of method onCreate with the instance of a new session table and a new fall table
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SESSION_TABLE_CREATE);
        db.execSQL(FALL_TABLE_CREATE);
    }

    //override of method onUpgrade with elimination of the tables and creation of new ones
    @Override
    public void onUpgrade(SQLiteDatabase db, int OldVersion, int NewVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FALL_TABLE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + SESSION_TABLE + ";");
        onCreate(db);
    }
}
