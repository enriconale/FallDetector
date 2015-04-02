package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;

import java.util.ArrayList;

/**
 * @author Enrico Naletto
 * Singleton class to manage the list of sessions and the currently active session.
 */
public class SessionsLab {
    private static SessionsLab sSessionsLab;
    private Context mAppContext;
    private ArrayList<Session> mSessionsList;
    private Session mRunningSession;

    private SessionsLab(Context appContext) {
        mAppContext = appContext;
        //Creates ten fake sessions to show them in cardview
        mSessionsList = new ArrayList<Session>();
    }

    public static SessionsLab get(Context c) {
        if (sSessionsLab == null) {
            sSessionsLab = new SessionsLab(c.getApplicationContext());
        }
        return sSessionsLab;
    }

    public void createNewRunningSession(Session session) {
        mRunningSession = session;
    }

    public ArrayList<Session> getSessions() {
        return mSessionsList;
    }

    public Session getRunningSession() {
        return mRunningSession;
    }

}
