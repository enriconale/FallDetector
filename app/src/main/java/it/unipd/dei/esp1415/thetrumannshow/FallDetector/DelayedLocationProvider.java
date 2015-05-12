package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Copyright Eike Trumann 12.05.15
 * All rights reserved
 */

public class DelayedLocationProvider implements LocationListener{
    private final Fall mFall;
    private final GoogleApiClient mGoogleApiClient;
    public DelayedLocationProvider(Fall f, GoogleApiClient apiClient){
        mFall = f;
        mGoogleApiClient = apiClient;

        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                this);

    }

    @Override
    public void onLocationChanged(Location location) {
        mFall.setLocation(location);
        unregisterListener();
    }

    private void unregisterListener(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
}
