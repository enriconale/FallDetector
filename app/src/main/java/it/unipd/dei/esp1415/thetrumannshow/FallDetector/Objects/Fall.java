package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects;

import android.location.Location;

import java.util.Date;

/**
 * @author Enrico Naletto
 */
public class Fall {
    private final String mFallName;
    private final Date mDate;
    private Double mLatitude;
    private Double mLongitude;
    private final float[] mXAcceleration;
    private final float[] mYAcceleration;
    private final float[] mZAcceleration;

    private Fall(Builder builder) {
        mFallName = builder.mFallName;
        mDate = builder.mDate;
        mLatitude = builder.mLatitude;
        mLongitude = builder.mLongitude;
        mXAcceleration = builder.mXAcceleration;
        mYAcceleration = builder.mYAcceleration;
        mZAcceleration = builder.mZAcceleration;
    }

    public String getName() {
        return mFallName;
    }

    public Date getDate() {
        return mDate;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public float[] getXAcceleration() {
        return mXAcceleration;
    }

    public float[] getYAcceleration() {
        return mYAcceleration;
    }

    public float[] getZAcceleration() {
        return mZAcceleration;
    }

    public void setLocation(Location loc) {
        if (loc != null) {
            mLatitude = loc.getLatitude();
            mLongitude = loc.getLongitude();
        } else {
            mLatitude = null;
            mLongitude = null;
        }
    }


    public static class Builder {
        private String mFallName;
        private Date mDate;
        private Double mLatitude;
        private Double mLongitude;
        private float[] mXAcceleration;
        private float[] mYAcceleration;
        private float[] mZAcceleration;

        public Builder() {
            mFallName = "";
            mDate = new Date();
            mLatitude = null;
            mLongitude = null;
            mXAcceleration = new float[1];
            mYAcceleration = new float[1];
            mZAcceleration = new float[1];
        }

        public Builder fallName(String val) {
            mFallName = val;
            return this;
        }

        public Builder date(Date val) {
            mDate = val;
            return this;
        }

        public Builder latitude(Double val) {
            mLatitude = val;
            return this;
        }

        public Builder longitude(Double val) {
            mLongitude = val;
            return this;
        }

        public Builder xAcceleration(float[] val) {
            mXAcceleration = val;
            return this;
        }

        public Builder yAcceleration(float[] val) {
            mYAcceleration = val;
            return this;
        }

        public Builder zAcceleration(float[] val) {
            mZAcceleration = val;
            return this;
        }

        public Fall build() {
            return new Fall(this);
        }
    }
}
