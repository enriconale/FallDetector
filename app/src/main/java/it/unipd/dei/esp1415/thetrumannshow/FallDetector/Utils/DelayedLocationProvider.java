package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Objects.Fall;

/**
 * Copyright Eike Trumann 12.05.15
 * All rights reserved
 *
 * The Delayed Location Provider requests a location from Googles FusedLocationProvider.
 * It is meant to use its own thread to not delay other parts of the application.
 */

public class DelayedLocationProvider implements LocationListener{
    private final Fall mFall;
    private final GoogleApiClient mGoogleApiClient;
    private final FallObjectCreator mFallObjectCreator;
    private Context mAppContext;

    public DelayedLocationProvider(Fall f, GoogleApiClient apiClient, FallObjectCreator foc,
                                   Context appContext){
        mFall = f;
        mGoogleApiClient = apiClient;
        mFallObjectCreator = foc;
        mAppContext = appContext;

        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mFall.setLocation(location);
        SessionsLab lab = SessionsLab.get(mAppContext);
        lab.getRunningSession().addFall(mFall);
        lab.saveFallInDatabase(mFall);
        unregisterListener();

        mFallObjectCreator.locationFixed();
    }

    private void unregisterListener(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
}
