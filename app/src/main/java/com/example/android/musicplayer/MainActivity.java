package com.example.android.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.concurrent.TimeUnit;



public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    // This takes care of the progress bar. It shows the current position of the song.
    private SeekBar seekBar;
    private Handler durationHandler = new Handler();
    AudioManager am;

    /**
     * This takes care of change of Audio focus. It alerts me when I lose or gain Audio focus
     */
    private AudioManager.OnAudioFocusChangeListener afChangeListener =
            new AudioManager.OnAudioFocusChangeListener(){
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT||
                    focusChange==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK||
                    focusChange == AudioManager.AUDIOFOCUS_LOSS){
                //Pause playback because Audio Focus is temporarily unavailable
                //Like answering a phone call
                mediaPlayer.pause();
            }
            else if(focusChange==AudioManager.AUDIOFOCUS_GAIN){
                mediaPlayer.start();
            }
        }
    };

    /**
     * This describes the action to be performed when the song is completed.
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            //When the song is completed, I want it to start all over.
            //Hence, I replicated the play button action here.
            startPlayer();
        }
    };

    /**
     * Line of commands that start the song
     */
    private void startPlayer() {
        // I first request for Audio focus.
        int result = am.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        // If my request is granted, I then play audio
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Yea, display a toast message that says play
            Toast.makeText(MainActivity.this, "Play", Toast.LENGTH_SHORT).show();
            // You can start the audio please
            mediaPlayer.start();
            // Yea, this updates my seekBar please. Every 1 second
            durationHandler.postDelayed(updateSeekBarTime, TimeUnit.SECONDS.toMillis(1));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // This gets the audio service so I can implement Audio focus
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // This locates the playPause button
        final ToggleButton playPause = (ToggleButton) findViewById(R.id.play_pause);
        // This function initializes my variables
        initializeViews();

        // This takes care of taking my music to current time as seek bar is moved.
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    //Take the media to the current position. Thanks
                    mediaPlayer.seekTo(progress);
                }
            }

            // This override method is a must. However, I have nothing to do here.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing to do here
            }

            // This override method is a must. However, I have nothing to do here.
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing to do here too
            }
        });


        // What happens when play_pause button is clicked.
        // Please check what state the button is. On or Off?
        playPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If in On state; that is play state, please play.
                if (isChecked){
                   startPlayer();
                    // Else pause.
                } else mediaPlayer.pause();
            }
        });

        // You guessed right. It is what happens when the stop button is clicked.
        ImageButton stopButton = (ImageButton) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display a toast that shows that stop button has been pressed.
                Toast.makeText(MainActivity.this,"Stop",Toast.LENGTH_SHORT).show();
                // Yea, this is my unique code to do that.
                // First pause the song.
                // Then take it back to ground zero. Cool right!
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);

                // Toggle the state of the playPause button.
                if(playPause.isChecked()){
                    playPause.toggle();
                }
            }
        });

        // What happens when loop button is clicked.
        ToggleButton loop = (ToggleButton) findViewById(R.id.loop);
        // Please check what state the button is. On or Off?
        loop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If in On state, please repeat.
                if (isChecked){
                    mediaPlayer.setOnCompletionListener(onCompletionListener);
                } // Else, do nothing. Yea.
            }
        });
    }


    // I initialize my variables here.
    private void initializeViews() {
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_song);
        double finalTime;
        finalTime = mediaPlayer.getDuration();
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setMax((int) finalTime);
        seekBar.setClickable(true);
    }

    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            double timeElapsed;
            //get current position
            timeElapsed = mediaPlayer.getCurrentPosition();
            //set seekBar progress
            seekBar.setProgress((int) timeElapsed);
            durationHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
        }
    };

    private void releaseMediaPlayer(){
        if(mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer=null;
            am.abandonAudioFocus(afChangeListener);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        releaseMediaPlayer();
    }
}
