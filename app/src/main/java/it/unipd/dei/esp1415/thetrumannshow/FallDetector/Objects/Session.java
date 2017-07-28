package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.CurrentLocale;

/**
 * @author Enrico Naletto
 *         Class that represents a Session object
 */
public class Session {
    private UUID mUUID;
    private String mSessionName;
    private Date mStartDate;
    private long mDuration;
    private LinkedList<Fall> mFalls;
    private int mIconColor;
    private int mNumberOfFalls;

    private Session(Builder builder) {
        mUUID = builder.mUUID;
        mSessionName = builder.mSessionName;
        mStartDate = builder.mStartDate;
        mFalls = builder.mFalls;
        mDuration = builder.mDuration;
        mIconColor = builder.mIconColor;
        mNumberOfFalls = builder.mNumberOfFalls;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public String getSessionName() {
        return mSessionName;
    }

    public String getFormattedSessionName() {
        int maxCharactersToShow = 30;
        if (mSessionName.length() > maxCharactersToShow) {
            return mSessionName.substring(0, maxCharactersToShow) + "...";
        }

        return mSessionName;
    }

    public Date getDate() {
        return mStartDate;
    }

    public long getDuration() {
        return mDuration;
    }

    public int getIconColorRgbValue() {
        return mIconColor;
    }

    public String getFormattedDuration() {
        int seconds = (int) (mDuration / (1000) % 60);
        int minutes = (int) ((mDuration / (1000 * 60)) % 60);
        int hours = (int) ((mDuration / (1000 * 60 * 60)) % 24);
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

    public void setListOfFalls(LinkedList<Fall> list) {
        mFalls = list;
        mNumberOfFalls = mFalls.size();
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public static class Builder {
        private UUID mUUID;
        private String mSessionName;
        private Date mStartDate;
        private LinkedList<Fall> mFalls;
        private long mDuration;
        private int mIconColor;
        private int mNumberOfFalls;

        public Builder() {
            mUUID = UUID.randomUUID();
            mSessionName = "";
            mStartDate = new Date();
            mDuration = 0;
            mFalls = new LinkedList<>();
            mIconColor = IconColor.getRandomColor();
            mNumberOfFalls = 0;
        }

        public Builder UUID(UUID val) {
            mUUID = val;
            return this;
        }

        public Builder sessionName(String val) {
            mSessionName = val;
            return this;
        }

        public Builder startDate(Date val) {
            mStartDate = val;
            return this;
        }

        public Builder falls(LinkedList<Fall> val) {
            mFalls = val;
            mNumberOfFalls = mFalls.size();
            return this;
        }

        public Builder duration(long val) {
            mDuration = val;
            return this;
        }

        public Builder iconColor(int val) {
            mIconColor = val;
            return this;
        }

        public Builder numberOfFalls(int val) {
            mNumberOfFalls = val;
            return this;
        }

        public Session build() {
            return new Session(this);
        }
    }
}
