package com.example.sphan.urbannoise;

import android.util.Log;

/**
 * Created by amac868 on 21/11/2015.
 */
public class SoundMeter {
    private static SoundMeter ourInstance = new SoundMeter();
    private final static String TAG = SoundMeter.class.getSimpleName();

    //filter coefficients must match the sampling rate
    //Sampling at 16kHz gives spectral information for 0-8kHz
    private final static int SAMPLE_RATE = 16000;

    //filter coefficients
    private static final double[] a = {0.0, 2.1856, -0.7403, -1.0831, 0.6863, -0.2274, 0.2507,
            -0.0058, -0.0821, 0.0153, 0.0004};
    private static final double[] b = {0.9299, -2.1889, 0.7541, 1.3229, -0.7728, 0.1025, -0.2398,
            -0.0098, 0.1154, -0.0103, -0.0033};

    //10th order filter
    private static final int BUFF_LEN = 11;
    private double[] v_A;
    private double[] v;
    private double offset;
    private int buffptr;
    private int sampleCount;
    private double avg_sq;
    double LA_eq;

    //store SECONDS seconds worth of sound measurements
    private static final int SECONDS = 120;
    private double[] soundMeasurement;

    public static SoundMeter getInstance() {
        return ourInstance;
    }

    private SoundMeter() {
        v_A = new double[BUFF_LEN];
        v = new double[BUFF_LEN];
        for(int i = 0; i < BUFF_LEN; i++) {
            v[i] = 0.0;
            v_A[i] = 0.0;
        }
        buffptr = 0;
        sampleCount = 0;
        avg_sq = 0.0;
        LA_eq = 0.0;
        offset = 0.0;
        soundMeasurement = new double[SECONDS];
        for(int i = 0; i < SECONDS; i++) {
            soundMeasurement[i] = 0.0;
        }
    }

    //Stores the last SECONDS recorded sound levels
    public void logSound(short[] sData) {
        int i = 0;

        for(; i < sData.length; i++) {
            v[buffptr] = sData[i];
            double original= b[0]*v[buffptr], filtered = 0.0;
            for(int k = 1; k < BUFF_LEN; k++) {
                original += b[k]*v[(BUFF_LEN + buffptr - k)%BUFF_LEN];
                filtered += (a[k]*v_A[(BUFF_LEN + buffptr - k)%BUFF_LEN]);
            }
            v_A[buffptr] = original-filtered;
            sampleCount++;
            avg_sq =(avg_sq*(sampleCount-1)+v_A[buffptr]*v_A[buffptr])/sampleCount;
            if(sampleCount >= SAMPLE_RATE) {
                LA_eq = 10*Math.log10(avg_sq) + offset;
                int j = soundMeasurement.length-1;
                while(j > 0) {
                    soundMeasurement[j] = soundMeasurement[j-1];
                    j--;
                }
                soundMeasurement[0] = LA_eq;
                avg_sq = 0.0;
                sampleCount = 0;
                Log.d(TAG, "Sound level: " + LA_eq + " dBA");
            }
        }
    }

    public void clear() {
        for(int i = 0; i < BUFF_LEN; i++) {
            v[i] = 0.0;
            v_A[i] = 0.0;
        }
        buffptr = 0;
        sampleCount = 0;
        avg_sq = 0.0;
        LA_eq = 0.0;
        offset = 0.0;
        for(int i = 0; i < SECONDS; i++) {
            soundMeasurement[i] = 0.0;
        }
    }

    //Get the last n measurements
    public double[] getMeasurements(int n) {
        double[] m = new double[Math.min(n,SECONDS)];
        for(int i = 0; i < m.length; i++) {
            m[i] = soundMeasurement[i];
        }
        return m;
    }

    //Get the nth most recent measurement, defaults to oldest recorded for n > buffer length
    public double getMeasurment(int n) {
        return soundMeasurement[Math.min(n,SECONDS)];
    }
}
