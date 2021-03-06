package com.example.sphan.urbannoise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;

public class MyLocationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = MyLocationActivity.class.getSimpleName();
    private static final long LOCATION_UPDATE_INTERVAL = 5 * 1000; // 5 milliseconds

    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mLastUpdatedTime;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView timeLastUpdatedTextView;
    private TextView MGRSTextView;

    private boolean mRequestingLocationUpdates;
    private boolean gpsEnabled;
    private boolean networkEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        updateValuesFromBundle(savedInstanceState);

        buildGoogleApiClient();

        latitudeTextView = (TextView) findViewById(R.id.latitudeTextview);
        longitudeTextView = (TextView) findViewById(R.id.longitudeTextview);
        timeLastUpdatedTextView = (TextView) findViewById(R.id.lastUpdatedTextView);
        MGRSTextView = (TextView) findViewById(R.id.MGRSTextview);
        mRequestingLocationUpdates = true;
        gpsEnabled = false;
        networkEnabled = false;

        mGoogleApiClient.connect();

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mGoogleApiClient.isConnected() == false)
        {
            mGoogleApiClient.connect();
        }

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.i(TAG, "Location service connected");
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Toast.makeText(this, "GoogleApiClient connected", Toast.LENGTH_LONG).show();

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
            mLastUpdatedTime = DateFormat.getDateTimeInstance().format(new Date());
            updateUI();
        }

        if (mRequestingLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failure : " + connectionResult.toString(), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Connection Failure : " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();

//        if (connectionResult.hasResolution()) {
//            try {
//                // Start an Activity that tries to resolve the error
//                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//            } catch (IntentSender.SendIntentException e) {
//                e.printStackTrace();
//            }
//        } else {
//            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
//        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        Log.d(TAG, "location is enabled? " + isLocationEnabled(this));

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gpsEnabled == false)
            {
                showLocationPermissionDialog(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdatedTime = DateFormat.getDateTimeInstance().format(new Date());

        Log.d(TAG, "my location: " + mCurrentLocation.toString());
        updateUI();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdatedTime);
        super.onSaveInstanceState(savedInstanceState);
    }

//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected() == true)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdatedTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }

            updateUI();
        }
    }

    private void showLocationPermissionDialog(final String settingDestination)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(this.getResources().getString(R.string.gps_network_not_enabled));
        dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(settingDestination);
                startActivity(myIntent);
                //get gps
            }
        });
        dialog.setNegativeButton(this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }

    private void updateUI() {
        if (mCurrentLocation != null)
        {
            latitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
            longitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
            timeLastUpdatedTextView.setText(String.valueOf(mLastUpdatedTime));
            MGRSTextView.setText(new CoordConverter(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()).getMGRS());
        }
        else
        {
            Log.d(TAG, "my location is null");
        }

//        mLastUpdateTimeTextView.setText(mLastUpdateTime);
    }

}