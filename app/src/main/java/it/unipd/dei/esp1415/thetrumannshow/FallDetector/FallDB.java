package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by alessandro on 05/05/15.
 */
public class FallDB extends SQLiteOpenHelper{

    private static final String db_name="Fall Database.db";
    private static final int db_version=3;

    public static final String session_table="Session";
    public static final String id_session="ID";
    public static final String name_session="Name";
    public static final String date_session="Date";
    public static final String duration_session="Duration";
    public static final String color1_icon_session="Color 1 Icon Session";
    public static final String color2_icon_session="Color 2 Icon Session";
    public static final String color3_icon_session="Color 3 Icon Session";

    public static final String fall_table="Fall";
    public static final String name_fall="Name";
    public static final String date_fall="Date";
    public static final String location_fall="Location";
    public static final String x_acceleration="X Acceleration";
    public static final String y_acceleration="Y Acceleration";
    public static final String z_acceleration="Z Acceleration";
    public static final String email_sent_fall="Email Sent?";


    public FallDB(Context context) {
        super(context, db_name, null, db_version);
    }

        @Override
        public void onCreate(SQLiteDatabase db){
            String sql1= "Create table " + session_table +" ( " +  id_session + " integer primary key, " + name_session + " text not null, " + date_session + " text not null, " + duration_session + " integer not null, " + color1_icon_session + " integer not null, " + color2_icon_session + " integer ot null," + color3_icon_session +" integer not null" + email_sent_fall + " integer not null);";
            String sql2="Create table " + fall_table + " ( " + name_fall + " text primary key, " + date_fall + " text not null, " + location_fall + " text not null, " + x_acceleration + " integer not null, " + y_acceleration + " integer not null, " + z_acceleration + "integer not null);";
            db.execSQL(sql1);
            db.execSQL(sql2);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int OldVersion, int NewVersion){
            String sql = null;
        }
}
