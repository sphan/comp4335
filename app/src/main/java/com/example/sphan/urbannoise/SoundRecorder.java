package com.example.sphan.urbannoise;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Created by sphan on 11/11/2015.
 * code from http://stackoverflow.com/questions/8499042/android-audiorecord-example/13487250#13487250
 */
public class SoundRecorder {
    private static SoundRecorder ourInstance = new SoundRecorder();
    private final static String TAG = SoundRecorder.class.getSimpleName();

    private static final int RECORDER_SAMPLE_RATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    private int BufferElement2Rec = 1024;
    private int BytesPerElement = 2;

    private MediaRecorder myAudioRecorder;
    private String outputFile;
    private int bufferSize;

//    //filter coefficients
//    private static final double[] a = {0.0, 2.1856, -0.7403, -1.0831, 0.6863, -0.2274, 0.2507,
//            -0.0058, -0.0821, 0.0153, 0.0004};
//    private static final double[] b = {0.9299, -2.1889, 0.7541, 1.3229, -0.7728, 0.1025, -0.2398,
//            -0.0098, 0.1154, -0.0103, -0.0033};
//
//    //10th order filter
//    private static final int BUFF_LEN = 11;
//    private double[] v_A;
//    private double[] v;
//    private double offset;
//    private int buffptr;
//    private int sampleCount;
//    private double avg_sq;
//    double LA_eq;
//
//    //store SECONDS seconds worth of sound measurements
//    private static final int SECONDS = 120;
//    private double[] soundMeasurement;
    private static SoundMeter soundMeter = SoundMeter.getInstance();

    public static SoundRecorder getInstance() {
        return ourInstance;
    }

    public void startRecording()
    {
//        try
//        {
//            Log.d(TAG, "start recording using MIC");
//            myAudioRecorder.prepare();
//            myAudioRecorder.start();
//            Log.d(TAG, "recording started");
//        }
//        catch (IllegalStateException e)
//        {
//            Log.d(TAG, "IllegalStateException happened");
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            Log.d(TAG, "IOException happned");
//            e.printStackTrace();
//        }

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, BufferElement2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public void stopRecording()
    {
//        myAudioRecorder.stop();
//        myAudioRecorder.release();
//        myAudioRecorder = null;

        if (recorder != null)
        {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }

    }

    public void playRecording()
    {
//        MediaPlayer mediaPlayer = new MediaPlayer();
//
//        try
//        {
//            mediaPlayer.setDataSource(outputFile);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//        try
//        {
//            mediaPlayer.prepare();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//
//        mediaPlayer.start();

        File file = new File(outputFile);
        int shortSizeInBytes = Short.SIZE/Byte.SIZE;
        int bufferSizeInBytes = (int) (file.length()/shortSizeInBytes);
        byte[] audioData = new byte[bufferSizeInBytes];

        try
        {
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, RECORDER_AUDIO_ENCODING, bufferSize, AudioTrack.MODE_STREAM);
            audioTrack.play();
            InputStream inputStream = new FileInputStream(file);

            int i = 0;
            while ((i = inputStream.read(audioData)) != -1)
            {
                audioTrack.write(audioData, 0, i);
            }

//            InputStream inputStream = new FileInputStream(file);
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
//
//            int i = 0;
//            while (dataInputStream.available() > 0)
//            {
//                audioData[i] = dataInputStream.readShort();
//                i++;
//            }
//
//            dataInputStream.close();
//
//


//            audioTrack.write(audioData, 0, bufferSizeInBytes);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private SoundRecorder() {
//        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice16K16bitmono.pcm";
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
//        v_A = new double[BUFF_LEN];
//        v = new double[BUFF_LEN];
//        for(int i = 0; i < BUFF_LEN; i++) {
//            v[i] = 0.0;
//            v_A[i] = 0.0;
//        }
//        buffptr = 0;
//        sampleCount = 0;
//        avg_sq = 0.0;
//        LA_eq = 0.0;
//        offset = 0.0;
//        soundMeasurement = new double[SECONDS];
//        for(int i = 0; i < SECONDS; i++) {
//            soundMeasurement[i] = 0.0;
//        }
        createMediaRecorder();
    }

    private void createMediaRecorder()
    {
        Log.d(TAG, "start creating MediaRecorder");
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);
        Log.d(TAG, "finish creating MediaRecorder");
    }

    private void writeAudioDataToFile()
    {
//        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice8K16bitmono.pcm";
        short sData[] = new short[BufferElement2Rec];
//        byte bData[] = new byte[BufferElement2Rec * 2];

        FileOutputStream os = null;
        try
        {
            os = new FileOutputStream(outputFile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        while (isRecording == true)
        {
            recorder.read(sData, 0, BufferElement2Rec);
            Log.d(TAG, "byte writing to file " + sData.toString());
            soundMeter.logSound(sData);

/*            int i = 0;

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
                if(sampleCount >= RECORDER_SAMPLE_RATE) {
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
            }*/
            try
            {
                byte bData[] = short2byte(sData);
                os.write(bData, 0, BufferElement2Rec * BytesPerElement);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        try {
            os.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private byte[] short2byte(short[] sData)
    {
        int shortArrSize = sData.length;
        byte[] bytes = new byte[shortArrSize * 2];
        for (int i = 0; i < shortArrSize; i++)
        {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

    public void startMeasurment() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, BufferElement2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                measureSound();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public void measureSound() {
        short[] sData = new short[BufferElement2Rec];
        while(isRecording) {
            recorder.read(sData, 0, BufferElement2Rec);
            soundMeter.logSound(sData);
        }
    }

    public double getMeasurement() {
        return soundMeter.getMeasurment(0);
    }

    public void resetMeter() {
        soundMeter.clear();
    }

//    public double[] getMeasurements(int nSeconds) {
//        double[] m = new double[Math.min(nSeconds,SECONDS)];
//        for(int i = 0; i < m.length; i++) {
//            m[i] = soundMeasurement[i];
//        }
//        return m;
//    }
//
//    public double getMeasurment(int secondsAgo) {
//        return soundMeasurement[Math.min(secondsAgo,SECONDS)];
//    }
}
