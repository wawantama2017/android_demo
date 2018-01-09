package com.example.ccsph2.mediaplayerdemo;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Broadcast intent
    public static final String BC_PLAY_AUDIO_WARIH = "com.example.ccsph2.mediaplayerdemo.PlayAudioWarih";
    public static final String BC_STOP_AUDIO_WARIH = "com.example.ccsph2.mediaplayerdemo.StopAudioWarih";
    public static final String BC_PAUSE_AUDIO_WARIH = "com.example.ccsph2.mediaplayerdemo.PauseAudioWarih";
    public static final String BC_RESUME_AUDIO_WARIH = "com.example.ccsph2.mediaplayerdemo.ResumeAudioWarih";

    // MediaPlayer service
    private MediaPlayerService mPlayer;
    private boolean bServiceConnected = false;

    // Storage access
    private ArrayList<ContentData> mContentList;
    private int mContentIndex;
    private ContentData mActiveContent;

    // Video View
    private VideoView mVideoView;

    // Seek bar
    private SeekBar mContentSeekBar;
    private long mUpdatePeriod = 100;

    // Handler for seek bar update
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Explicitly set permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }

        // Display Video View
        showVideoView();

        // Set TextView with Content Title
        showContentTitle();

        //playMedia();  // playMedia execution moved after VideoView prepared. to avoid muted.
                        // refer to onPrepared in VideoView
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bServiceConnected) {
            unbindService(mServiceConnection);
            //service is active
            //mPlayer.stopSelf();
        }
    }

    public void onClickStopButton(View view){
        stopMedia();
    }

    public void onClickPauseButton(View view){
        pauseMedia();
    }

    public void onClickResumeButton(View view){
        resumeMedia();
    }

    public void onClickBackButton(View view){
        mHandler.removeCallbacks(mTask);
        finish();
    }

    // Bind this activity to MediaPlayer service
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Local service bound, get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) iBinder;
            mPlayer = binder.getService();
            bServiceConnected = true;
            // For Debug show quick little message
            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bServiceConnected = false;
        }
    };

    // Play Audio
    private void playMedia () {

        // First bind
        if (!bServiceConnected) {
            // Throw intent
            Intent playerIntent = new Intent (this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        else {
            // Service already bound
            Intent broadcastIntent = new Intent(BC_PLAY_AUDIO_WARIH);
            sendBroadcast(broadcastIntent);
        }
    }

    private void stopMedia(){
        if (bServiceConnected) {
            mPlayer.stopMedia();
        } else {
            Intent broadcastIntent = new Intent(BC_STOP_AUDIO_WARIH);
            sendBroadcast(broadcastIntent);
        }
    }

    private void pauseMedia(){
        if (bServiceConnected) {
            mPlayer.pauseMedia();
        } else {
            Intent broadcastIntent = new Intent(BC_PAUSE_AUDIO_WARIH);
            sendBroadcast(broadcastIntent);
        }
    }

    private void resumeMedia(){
        if (bServiceConnected) {
            mPlayer.resumeMedia();
        } else {
            Intent broadcastIntent = new Intent(BC_RESUME_AUDIO_WARIH);
            sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Other private methods
     */

    private void showContentTitle() {
        // Load Data from SharedPreferences
        ContentDataStorage storage = new ContentDataStorage(getApplicationContext());
        mContentList = storage.loadContent();
        mContentIndex = storage.loadContentIndex();

        // Get active content
        mActiveContent = mContentList.get(mContentIndex);

        // Content content path
        TextView showTitle = (TextView) findViewById(R.id.textView2);
        final String title = mActiveContent.getTitle().toString();
        showTitle.setText(title);
    }

    private void showVideoView() {

        //String VideoURL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
        String VideoURL = "http://192.168.11.15/demo.mp4"; // my own server

        mVideoView = (VideoView) findViewById(R.id.videoView);

        //MediaController mediacontroller = new MediaController(MainActivity.this);
        //mediacontroller.setAnchorView(mVideoView);
        // Get the URL
        Uri video = Uri.parse(VideoURL);
        //mVideoView.setMediaController(mediacontroller);
        mVideoView.setVideoURI(video);

        mVideoView.clearFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                //pDialog.dismiss();
                mVideoView.start();

                // mute audio in VideoView
                mp.setVolume(0f, 0f);
                mp.setLooping(true);

                // play media here
                playMedia();
                prepareSeekBar();
                updateSeekBar();
            }
        });

    }

    /**
     * Seek Bar
     */

    private void prepareSeekBar() {
        mContentSeekBar = (SeekBar) findViewById(R.id.contentSeekBar);
        // Listeners
        mContentSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // remove message Handler from updating progress bar
                mHandler.removeCallbacks(mTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mTask);
                int duration = (int) mPlayer.getDuration();
                int currentPosition = progressToMsec(seekBar.getProgress(), duration);

                // forward or backward to certain seconds
                mPlayer.seekTo(currentPosition);

                // update timer progress again
                updateSeekBar();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });

        // Set Range
        mContentSeekBar.setProgress(0);
        mContentSeekBar.setMax(100);
    }

    private void updateSeekBar() {
        mHandler.postDelayed(mTask, mUpdatePeriod);
    }

    /**
     * Thread
     */

    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            long duration = mPlayer.getDuration();
            long currentPosition = mPlayer.getCurrentPosition();

            // Update progress bar
            int progress = getProgressPercentage(duration, currentPosition);
            mContentSeekBar.setProgress(progress);

            // Running this thread after designated period
            mHandler.postDelayed(this, mUpdatePeriod);
        }
    };

    /**
     * Utility
     */

    private int getProgressPercentage (long duration, long position) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (position / 1000);
        long totalSeconds = (int) (duration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    private int progressToMsec(int progress, int duration) {
        int currentPosition = 0;
        duration = duration / 1000;
        currentPosition = (int) ((((double)progress) / 100) * duration);

        // return current duration in milliseconds
        return currentPosition * 1000;
    }
}
