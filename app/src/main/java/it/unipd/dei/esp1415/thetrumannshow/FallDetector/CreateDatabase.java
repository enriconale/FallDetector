package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by alessandro on 05/05/15.
 */
public class CreateDatabase extends SQLiteOpenHelper{

    private static final String db_name = "Fall Database.db";
    private static final int db_version = 1;

    public static final String SESSION_TABLE = "Session";
    public static final String SESSION_ID = "id";
    public static final String SESSION_NAME = "name";
    public static final String SESSION_DATE = "date";
    public static final String SESSION_DURATION = "duration";
    public static final String SESSION_ICON_COLOR_1 = "icon_color_1";
    public static final String SESSION_ICON_COLOR_2 = "icon_color_2";
    public static final String SESSION_ICON_COLOR_3 = "icon_color_3";

    public static final String FALL_TABLE = "Fall";
    public static final String FALL_NAME = "name";
    public static final String FALL_DATE = "date";
    public static final String FALL_LOCATION = "location";
    public static final String X_ACCELERATION = "x_acceleration";
    public static final String Y_ACCELERATION = "y_acceleration";
    public static final String Z_ACCELERATION = "z_acceleration";
    public static final String EMAIL_SENT = "email_sent";
    public static final String OWNER_SESSION = "session_id";

    private static final String SESSION_TABLE_CREATE = "create table "
            + SESSION_TABLE + " ("
            + SESSION_ID + " text primary key, "
            + SESSION_NAME + " text, "
            + SESSION_DATE + " text, "
            + SESSION_DURATION + " integer, "
            + SESSION_ICON_COLOR_1 + " integer, "
            + SESSION_ICON_COLOR_2 + " integer, "
            + SESSION_ICON_COLOR_3 + " integer);";

    private static final String FALL_TABLE_CREATE = "create table "
            + FALL_TABLE + " ("
            + FALL_NAME + " text, "
            + FALL_DATE + " text, "
            + FALL_LOCATION + " text, "
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


    public CreateDatabase(Context context) {
        super(context, db_name, null, db_version);
    }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(SESSION_TABLE_CREATE);
            db.execSQL(FALL_TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int OldVersion, int NewVersion) {
            String sql = null;
            db.execSQL(sql);
        }
}
