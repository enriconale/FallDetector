package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrico Naletto
 */
public class SessionsLab {
    private static SessionsLab sSessionsLab;
    private Context mAppContext;
    private ArrayList<Session> mSessionsList;

    private SessionsLab(Context appContext) {
        mAppContext = appContext;
    }

    public static SessionsLab get(Context c) {
        if (sSessionsLab == null) {
            return new SessionsLab(c.getApplicationContext());
        }
        return sSessionsLab;
    }

    public ArrayList<Session> getSessions() {
        return mSessionsList;
    }
}
