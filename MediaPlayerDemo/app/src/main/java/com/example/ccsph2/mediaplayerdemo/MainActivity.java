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
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
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
    private ArrayList<ContentData> contentList;
    private int contentIndex = -1;
    private ContentData activeContent;

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


        showVideoView();
        //Thread.sleep(500);
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

    /*
     * Other private methods
     */

    private void showContentTitle() {
        // Load Data from SharedPreferences
        ContentDataStorage storage = new ContentDataStorage(getApplicationContext());
        contentList = storage.loadContent();
        contentIndex = storage.loadContentIndex();

        // Get active content
        activeContent = contentList.get(contentIndex);

        // Content content path
        TextView showTitle = (TextView) findViewById(R.id.textView2);
        final String title = activeContent.getTitle().toString();
        showTitle.setText(title);
    }

    VideoView videoview;
    private void showVideoView() {

        //String VideoURL = "http://www.androidbegin.com/tutorial/AndroidCommercial.3gp";
        String VideoURL = "http://192.168.11.15/demo.mp4"; // my own server

        videoview = (VideoView) findViewById(R.id.videoView);

        //MediaController mediacontroller = new MediaController(MainActivity.this);
        //mediacontroller.setAnchorView(videoview);
        // Get the URL
        Uri video = Uri.parse(VideoURL);
        //videoview.setMediaController(mediacontroller);
        videoview.setVideoURI(video);

        videoview.clearFocus();
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            // Close the progress bar and play the video
            public void onPrepared(MediaPlayer mp) {
                //pDialog.dismiss();
                videoview.start();

                // mute audio in VideoView
                mp.setVolume(0f, 0f);
                mp.setLooping(true);

                // play media here
                playMedia();
            }
        });

    }
}
