package com.example.sphan.urbannoise;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class UrbanNoiseActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private static final String TAG = UrbanNoiseActivity.class.getSimpleName();

//    private static final LatLng AUSTRALIA = new LatLng(35.3080, 149.1245);

    private GoogleApiClient mGoogleApiClient;
    //    private Location mLastLocation;
    private String mLastUpdatedTime;
    private String deviceID;
    private Location mCurrentLocation;
    private double mCurrentDecibels;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private Button startButton;
    private Button endButton;
    private Button viewOnMapButton;

    private ArrayList<Location> locations;
    private ArrayList<Double> decibels;
    private ArrayList<String> datetimes;


    private boolean mRequestingLocationUpdates;
    private boolean gpsEnabled;
    private boolean networkEnabled;
    private boolean isPaused;

    private int lastLocationIndex;
    private MyFusionTable ft = new MyFusionTable();

    private SoundRecorder soundMeter;

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
            mLastUpdatedTime = DateFormat.getDateTimeInstance().format(new Date());
            mCurrentDecibels = soundMeter.getMeasurement();
        }

        if (mRequestingLocationUpdates)
        {
            startLocationUpdates();
//            updateTable();
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



    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdatedTime = DateFormat.getDateTimeInstance().format(new Date());
        mCurrentDecibels = soundMeter.getMeasurement();

        locations.add(mCurrentLocation);
        datetimes.add(mLastUpdatedTime);
        decibels.add(soundMeter.getMeasurement());

        Log.d(TAG, "my location: " + mCurrentLocation.toString());
        updateTable(mCurrentLocation, mLastUpdatedTime, mCurrentDecibels);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(Constants.REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelableArrayList(Constants.LOCATION_KEY, locations);
        savedInstanceState.putStringArrayList(Constants.LAST_UPDATED_TIME_STRING_KEY, datetimes);
        savedInstanceState.putBoolean(Constants.IS_PAUSED_KEY, isPaused);
        savedInstanceState.putInt(Constants.LAST_LOCATION_INDEX_IN_LIST, locations.size());
        super.onSaveInstanceState(savedInstanceState);
    }

    public void startUrbanNoiseDetection(View view)
    {
        isPaused = false;
        startGetLocation();
        soundMeter.startRecording();
        startButton.setEnabled(isPaused);
        endButton.setEnabled(!isPaused);
    }

    public void stopUrbanNoiseDetection(View view)
    {
        isPaused = true;
        stopGetLocation();
        soundMeter.stopRecording();
        soundMeter.resetMeter();
        startButton.setEnabled(isPaused);
        endButton.setEnabled(!isPaused);
    }

    public void viewInMap(View view)
    {
        Intent intent = new Intent(this, MyMapActivity.class);
//        intent.putExtra("locations", locations);
//        intent.putExtra("decibels", decibels);
//        intent.putExtra("dateTimes", datetimes);
//        startActivity(intent);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult is called");
//        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "RETURNING FROM MAP VIEW");

        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                Bundle extras = data.getExtras();
                if (extras != null)
                {
                    deviceID = extras.getString("deviceID");
                    Log.d(TAG, "onActivityResult: deviceID: " + deviceID);
                }

//                startButton.setEnabled(isPaused);
//                endButton.setEnabled(isPaused);

                Log.d(TAG, "GETTING THE LOCATIONS, DECIBELS AND DATETIMES OBTAINED FROM MAP");

                locations = (ArrayList<Location>) data.getExtras().getSerializable("locations");
                decibels = (ArrayList<Double>) data.getExtras().getSerializable("decibels");
                datetimes = data.getExtras().getStringArrayList("dateTimes");

                if (locations == null)
                    Log.d(TAG, "LOCATIONS IS NULL");

                if (decibels == null)
                    Log.d(TAG, "DECIBELS IS NULL");

                if (datetimes == null)
                    Log.d(TAG, "DATETIMES IS NULL");

                if (locations != null && decibels != null && datetimes != null)
                {
                    for (int i = lastLocationIndex; i < locations.size(); ++i)
                    {
                        Log.d(TAG, "UPDATING THE TABLE");
                        updateTable(locations.get(i), datetimes.get(i), decibels.get(i));
                    }
                }
                else
                {
                    locations = new ArrayList<>();
                    decibels = new ArrayList<>();
                    datetimes = new ArrayList<>();
                }
//

            }
        }
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
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate is called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urban_noise);
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

        startButton = (Button) findViewById(R.id.start_button);
        endButton = (Button) findViewById(R.id.end_button);
        viewOnMapButton = (Button) findViewById(R.id.viewInMapButton);

        mRequestingLocationUpdates = true;
        gpsEnabled = false;
        networkEnabled = false;
        isPaused = true;

        lastLocationIndex = 0;

        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");
        datetimes = (ArrayList<String>) getIntent().getSerializableExtra("datetimes");
        decibels = (ArrayList<Double>) getIntent().getSerializableExtra("decibels");

        if (locations == null)
        {
            locations = new ArrayList<>();
        }

        if (datetimes == null)
        {
            datetimes = new ArrayList<>();
        }

        if(decibels == null) {
            decibels = new ArrayList<>();
        }
//        mGoogleApiClient.connect();

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            deviceID = extras.getString("deviceID");
        }

        soundMeter = SoundRecorder.getInstance();
        Log.d(TAG, "I'm here");
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (isPaused != true)
        {
            startGetLocation();
        }

//
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

        mLastUpdatedTime = DateFormat.getDateTimeInstance().format(new Date());
        mCurrentDecibels = soundMeter.getMeasurement();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constants.LOCATION_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.LOCATION_UPDATE_INTERVAL);
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
    protected void onPause() {
        super.onPause();

        stopGetLocation();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(Constants.REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        Constants.REQUESTING_LOCATION_UPDATES_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(Constants.LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                locations = savedInstanceState.getParcelableArrayList(Constants.LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(Constants.LAST_UPDATED_TIME_STRING_KEY)) {
                datetimes = savedInstanceState.getStringArrayList(Constants.LAST_UPDATED_TIME_STRING_KEY);
            }

            if (savedInstanceState.keySet().contains(Constants.IS_PAUSED_KEY))
            {
                isPaused = savedInstanceState.getBoolean(Constants.IS_PAUSED_KEY);
            }

            if (savedInstanceState.keySet().contains(Constants.LAST_LOCATION_INDEX_IN_LIST))
            {
                lastLocationIndex = savedInstanceState.getInt(Constants.LAST_LOCATION_INDEX_IN_LIST);
            }

            if (savedInstanceState.keySet().contains("deviceID"))
            {
                deviceID = savedInstanceState.getString("deviceID");
                Log.d(TAG, "deviceID: " + deviceID);
            }

            for (int i = 0; i < locations.size(); ++i)
            {
                updateTable(locations.get(i), datetimes.get(i), decibels.get(i));
                Log.d(TAG, "updating table for item #" + i);
            }
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

    private void updateTable(Location location, String dateTime, double decibels)
    {
        if (mCurrentLocation == null)
        {
            return;
        }

        LinearLayout dataTable = (LinearLayout) findViewById(R.id.dataTable);

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View row = layoutInflater.inflate(R.layout.row, null);

        TextView dateTimeTextView = (TextView) row.findViewById(R.id.dateTimeTextView);
        dateTimeTextView.setText(dateTime);

        CoordConverter coord = new CoordConverter(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        double[] gridpoint = coord.getGridpoint();
        TextView locationTextView = (TextView) row.findViewById(R.id.locationTextView);
        locationTextView.setText(String.valueOf(gridpoint[0]).substring(0,8) + "," +  String.valueOf(gridpoint[1]).substring(0,8));

        TextView dbmLevelTextView = (TextView) row.findViewById(R.id.dbmLevelTextView);
        dbmLevelTextView.setText(String.valueOf(decibels));

//        TextView deviceTextView = (TextView) row.findViewById(R.id.deviceTextView);
//        deviceTextView.setText(deviceID);

        dataTable.addView(row);
        ft.postRow(decibels,mCurrentLocation.getLongitude(),mCurrentLocation.getLatitude(),dateTime);
    }

    private void startGetLocation()
    {
        if (mGoogleApiClient == null)
        {
            return;
        }

        if (mGoogleApiClient.isConnected() == false)
        {
            mGoogleApiClient.connect();
        }

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates)
        {
            startLocationUpdates();
        }
    }

    private void stopGetLocation()
    {
        if (mGoogleApiClient == null)
        {
            return;
        }

        if (mGoogleApiClient.isConnected() == true)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

}
