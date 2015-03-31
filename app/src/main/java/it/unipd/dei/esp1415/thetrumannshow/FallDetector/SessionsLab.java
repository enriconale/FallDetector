package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;

import java.util.ArrayList;

/**
 * @author Enrico Naletto
 */
public class SessionsLab {
    private static SessionsLab sSessionsLab;
    private Context mAppContext;
    private ArrayList<Session> mSessionsList;

    private SessionsLab(Context appContext) {
        mAppContext = appContext;
        //Creates ten fake sessions to show them in cardview
        mSessionsList = new ArrayList<Session>();
        for (int i = 0; i <= 10; i++) {
            mSessionsList.add(new Session());
        }
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
