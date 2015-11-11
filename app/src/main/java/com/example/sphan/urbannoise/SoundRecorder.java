package com.example.sphan.urbannoise;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.IOException;

/**
 * Created by sphan on 11/11/2015.
 */
public class SoundRecorder {
    private static SoundRecorder ourInstance = new SoundRecorder();

    private MediaRecorder myAudioRecorder;
    private String outputFile;

    public static SoundRecorder getInstance() {
        return ourInstance;
    }

    public void startRecording()
    {
        try
        {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void stopRecording()
    {
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;
    }

    public void playRecording()
    {
        MediaPlayer mediaPlayer = new MediaPlayer();

        try
        {
            mediaPlayer.setDataSource(outputFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            mediaPlayer.prepare();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    private SoundRecorder() {
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
        createMediaRecorder();
    }

    private void createMediaRecorder()
    {
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);
    }

}
