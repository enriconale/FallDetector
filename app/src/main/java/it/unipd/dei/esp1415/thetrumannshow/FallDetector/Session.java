package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
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
    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mNumberOfFalls;

    public Session() {
        mUUID = UUID.randomUUID();
        mSessionName = "New Session";
        mStartDate = new Date();
        mDuration = 0;
        mFalls = new LinkedList<Fall>();
        mColor1 = (int)(Math.random() * 256);
        mColor2 = (int)(Math.random() * 256);
        mColor3 = (int)(Math.random() * 256);
        mNumberOfFalls = 0;
    }

    public Session(UUID uuid, String sessionName, Date date, long duration, int color1,
                   int color2, int color3, int numberOfFalls) {
        mUUID = uuid;
        mSessionName = sessionName;
        mStartDate = date;
        mDuration = duration;
        mFalls = new LinkedList<Fall>();
        mColor1 = color1;
        mColor2 = color2;
        mColor3 = color3;
        mNumberOfFalls = numberOfFalls;
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

    public int getColor1() {return mColor1;}

    public int getColor2() {return mColor2;}

    public int getColor3() {return mColor3;}

    public String getFormattedDuration() {
        int seconds = (int) (mDuration / 1000) % 60;
        int minutes = (int) ((mDuration / (1000*60)) % 60);
        int hours   = (int) ((mDuration / (1000*60*60)) % 24);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public LinkedList<Fall> getFalls() {
        return mFalls;
    }

    public int getNumberOfFalls() {
        return mNumberOfFalls;
    }

    public void setSessionName(String newName) {
        mSessionName = newName;
    }

    public void addFall(Fall fall) {
        mFalls.add(fall);
        mNumberOfFalls++;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }
}
