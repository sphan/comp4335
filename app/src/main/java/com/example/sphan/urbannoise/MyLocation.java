package com.example.sphan.urbannoise;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by sphan on 11/11/2015.
 */
public class MyLocation implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final long LOCATION_UPDATE_INTERVAL = 5 * 1000; // 5 milliseconds
    private static final String TAG = MyLocation.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
//    private Location mLastLocation;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;

    public Location getLocation()
    {
        return mCurrentLocation;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location service connected");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        Toast.makeText(this, "GoogleApiClient connected", Toast.LENGTH_LONG).show();

        if (mCurrentLocation == null)
        {
            Log.i(TAG, "mCurrentLocation is null");
        }

        if (mGoogleApiClient.isConnected() == false)
        {
            Log.i(TAG, "mGoogleApiClient is not connected");
        }

        if (mLocationRequest == null)
        {
            Log.i(TAG, "mLocationRequest is null");
        }

        if (mCurrentLocation == null &&
                (mGoogleApiClient.isConnected() && mLocationRequest != null))
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else
        {
            updateUI();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, "my location: " + mCurrentLocation.toString());
        updateUI();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
//        Toast.makeText(this, "Connection Failure : " + connectionResult.toString(), Toast.LENGTH_LONG).show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateUI() {
        if (mCurrentLocation != null)
        {
//            latitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
//            longitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
        }
        else
        {
            Log.d(TAG, "my location is null");
        }

//        mLastUpdateTimeTextView.setText(mLastUpdateTime);
    }
}
