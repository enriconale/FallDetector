package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

/**
 * @author Enrico Naletto
 */
public class Session {
    private UUID mUUID;
    private String mSessionName;
    private Date mStartDate;
    private long mDuration;
    private LinkedList<Fall> mFalls;

    public Session() {
        mUUID = UUID.randomUUID();
        mSessionName = "New Session";
        mStartDate = new Date();
        mDuration = 0;
        mFalls = new LinkedList<Fall>();
    }

    public Session(UUID uuid, String sessionName, Date date, long duration) {
        mUUID = uuid;
        mSessionName = sessionName;
        mStartDate = date;
        mDuration = duration;
        mFalls = new LinkedList<Fall>();
    }

    public UUID getUUID() {
        return mUUID;
    }

    public String getSessionName() {
        return mSessionName;
    }

    public Date getDate() {
        return mStartDate;
    }

    public long getDuration() {
        return mDuration;
    }

    public String getFormattedDuration() {
        int seconds = (int) (mDuration / 1000) % 60;
        int minutes = (int) ((mDuration / (1000*60)) % 60);
        int hours   = (int) ((mDuration / (1000*60*60)) % 24);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public LinkedList<Fall> getFalls() {
        return mFalls;
    }

    public void setSessionName(String newName) {
        mSessionName = newName;
    }

    public void addFall(Fall fall) {
        mFalls.add(fall);
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }
}
