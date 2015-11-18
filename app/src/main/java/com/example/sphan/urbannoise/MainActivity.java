package com.example.sphan.urbannoise;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String deviceID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        try {
            int v = getPackageManager().getPackageInfo("com.google.android.gms", 0 ).versionCode;

            Log.d(MainActivity.class.getSimpleName(), "Google Play Service version: " + v);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        getDeviceID();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getCurrentLocation(View view)
    {
        Intent intent = new Intent(this, MyLocationActivity.class);
        startActivity(intent);
    }

    public void recordSound(View view)
    {
        Intent intent = new Intent(this, SoundRecordingActivity.class);
        startActivity(intent);
    }

    public void getDeviceID(View view)
    {
        getDeviceID();
    }

    public void goToMap(View view)
    {
        Intent intent = new Intent(this, MyMapActivity.class);
        intent.putExtra("deviceID", deviceID);
        startActivity(intent);
    }

    public void startUrbanNoiseDetection(View view)
    {
        Intent intent = new Intent(this, UrbanNoiseActivity.class);
        intent.putExtra("deviceID", deviceID);
        startActivity(intent);
    }

    private void getDeviceID()
    {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidID;

        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidID = "" + android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        long tmSerialHash = 0;
        if (tmSerial != null)
        {
            tmSerialHash = (long) tmSerial.hashCode();
        }
        UUID deviceUUID = new UUID(androidID.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerialHash);

        deviceID = deviceUUID.toString();

        Log.d(TAG, "device ID: " + deviceID);
    }
}
