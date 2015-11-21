package com.example.sphan.urbannoise;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
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

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, Constants.RECORDER_SAMPLE_RATE,
                Constants.RECORDER_CHANNELS, Constants.RECORDER_AUDIO_ENCODING, BufferElement2Rec * BytesPerElement);

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
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Constants.RECORDER_SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, Constants.RECORDER_AUDIO_ENCODING, bufferSize, AudioTrack.MODE_STREAM);
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
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/voice8K16bitmono.pcm";
        bufferSize = AudioRecord.getMinBufferSize(Constants.RECORDER_SAMPLE_RATE, Constants.RECORDER_CHANNELS, Constants.RECORDER_AUDIO_ENCODING);

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
            //Log.d(TAG, "byte writing to file " + sData.toString());
            soundMeter.logSound(sData);

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

        try
        {
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
