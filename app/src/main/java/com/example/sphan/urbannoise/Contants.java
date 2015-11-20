package com.example.sphan.urbannoise;

import android.media.AudioFormat;

/**
 * Created by sphan on 21/11/2015.
 */
public class Contants {

    // for location updates
    public static final long LOCATION_UPDATE_INTERVAL = 5 * 1000; // 5 milliseconds
    public final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    public final static String LOCATION_KEY = "location-key";
    public final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    public final static String IS_PAUSED_KEY = "is-paused-key";

    // Sound Recording
    public static final int RECORDER_SAMPLE_RATE = 8000;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    // API Key
    public static final String API_KEY = "AIzaSyC7lS2fQyY3lo7a66cMeHJWgznoa-TFMdg";

    // OAUTH 2.0 Client ID
    public static final String OAUTH_CLIENT_ID = "674752158693-hkna4t7ld9oduv2unl0nfj6h3hlost97.apps.googleusercontent.com";\
    public static final String OAUTH_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    public static final String OAUTH_TOKEN_URI = "https://accounts.google.com/o/oauth2/token";

    // Service account Client ID
    public static final String SERVICE_ACC_PRIVATE_KEY_ID = "5dc3b6e2e2c36d3ab076d6430f78a8758007da83";
    public static final String SERVICE_ACC_CLIENT_EMAIL = "account-1@certain-wharf-112411.iam.gserviceaccount.com";
    public static final String SERVICE_ACC_CLIENT_ID = "104963371790041664474";
    public static final String SERVICE_ACC_AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    public static final String SERVICE_ACC_TOKEN_URI = "https://accounts.google.com/o/oauth2/token";

    public static final String FUSION_TABLE_READ_WRITE_SCOPE = "https://www.googleapis.com/auth/fusiontables";


}
