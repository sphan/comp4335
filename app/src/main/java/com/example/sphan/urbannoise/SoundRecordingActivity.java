package com.example.sphan.urbannoise;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SoundRecordingActivity extends AppCompatActivity {

    private Button startRecordingButton;
    private Button stopRecordingButton;
    private Button startPlayingButton;
    private Button stopPlayingButton;
    private SoundRecorder mySoundRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_recording);
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

        startRecordingButton = (Button) findViewById(R.id.start_record_button);

        stopRecordingButton = (Button) findViewById(R.id.stop_record_button);
        stopRecordingButton.setEnabled(false);

        startPlayingButton = (Button) findViewById(R.id.start_play_button);
        startPlayingButton.setEnabled(false);

        stopPlayingButton = (Button) findViewById(R.id.stop_play_button);
        stopPlayingButton.setEnabled(false);

        mySoundRecorder = SoundRecorder.getInstance();
    }

    public void startRecording(View view)
    {
        mySoundRecorder.startRecording();
        stopRecordingButton.setEnabled(true);
        startRecordingButton.setEnabled(false);
    }

    public void stopRecording(View view)
    {
        mySoundRecorder.stopRecording();
        stopRecordingButton.setEnabled(false);
        startPlayingButton.setEnabled(true);
        startRecordingButton.setEnabled(true);
    }

    public void startPlaying(View view)
    {
        mySoundRecorder.playRecording();
        startRecordingButton.setEnabled(false);
        stopPlayingButton.setEnabled(true);
        startPlayingButton.setEnabled(false);
    }

    public void stopPlaying(View view)
    {
        startRecordingButton.setEnabled(true);
        stopPlayingButton.setEnabled(false);
        startPlayingButton.setEnabled(false);
    }
}
