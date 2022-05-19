package com.example.dyplomapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class CamertoneActivity extends AppCompatActivity {

    EditText freqtext;
    ImageView button;

    AudioTrack audioTrack;

    int duration = 2;
    int sampleRate = 44100;
    int numberOfSamples = duration * sampleRate;
    double[] sample = new double[numberOfSamples];
    byte[] generatedSound = new byte[2 * numberOfSamples];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camertone);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Tuning fork");

        freqtext = findViewById(R.id.freqvalue);
        freqtext.setActivated(false);
        button = findViewById(R.id.tune_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setImageResource(R.drawable.tuning_fork_pas);
                generateTone(Double.valueOf(freqtext.getText().toString()));
                playSound();
                button.setImageResource(R.drawable.tuning_fork_pas);
            }
        });

    }

    void generateTone(double frequency){
        for (int i = 0; i < numberOfSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequency));
        }
        int index = 0;
        for (double dVal : sample){
            short val = (short)(dVal * 32767);
            generatedSound[index++] = (byte)(val & 0x00ff);
            generatedSound[index++] = (byte)((val & 0xff00) >>> 8);
        }
    }

    void playSound(){
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSound.length,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSound, 0, generatedSound.length);
        audioTrack.play();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camertone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        switch (id){
            case android.R.id.home:
                this.finish();
                return true;

            case R.id.manual_b:
                intent = new Intent(this, ManualActivity.class);
                startActivity(intent);
                return true;

            case R.id.about_b:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            audioTrack.release();
        } catch (Exception e){}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            audioTrack.release();
        } catch (Exception e){}
    }
}