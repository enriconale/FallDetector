package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

/**
 * Copyright Eike Trumann 12.05.15
 * All rights reserved
 */

public class DelayedReverseGeocoder implements Runnable{
    private final Fall mFall;
    private final GoogleApiClient mGoogleApiClient;
    private final Context mContext;

    private List<Address> mAddresses;
    private Geocoder mGeocoder;

    public DelayedReverseGeocoder(Fall f, GoogleApiClient apiClient, Context c){
        mFall = f;
        mGoogleApiClient = apiClient;
        mContext = c;

        mGeocoder = new Geocoder(c, Locale.getDefault());

        Thread thread = new Thread(this);
        thread.run();
    }

    @Override
    public void run() {
        try {
            mAddresses = mGeocoder.getFromLocation(mFall.getLocation().getLatitude(),
                    mFall.getLocation().getLatitude(), 1);
        } catch (java.io.IOException e) {
            return;
        }

        if(!mAddresses.isEmpty()) {
            mFall.setAddress(mAddresses.get(0));
        }
    }
}
