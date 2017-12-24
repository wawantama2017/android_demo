package com.example.ccsph2.mediaplayerdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Broadcast intent
    public static final String BC_PLAY_AUDIO_WARIH = "com.example.ccsph2.mediaplayernissan4.PlayAudioWarih";
    public static final String BC_STOP_AUDIO_WARIH = "com.example.ccsph2.mediaplayernissan4.StopAudioWarih";
    public static final String BC_PAUSE_AUDIO_WARIH = "com.example.ccsph2.mediaplayernissan4.PauseAudioWarih";
    public static final String BC_RESUME_AUDIO_WARIH = "com.example.ccsph2.mediaplayernissan4.ResumeAudioWarih";

    // MediaPlayer service
    private MediaPlayerService mPlayer;
    private boolean bServiceConnected = false;

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bServiceConnected) {
            unbindService(mServiceConnection);
            //service is active
            mPlayer.stopSelf();
        }
    }

    public void onClickPlayButton(View view){

        // For debug check external storage path
        // File file = null;
        // file = Environment.getExternalStorageDirectory();
        // Log.v("WarihDebug", "root="+file.getAbsolutePath());

        // Play SD card content
        // playMedia("/storage/emulated/0/sample0.mp3");
        playMedia("/storage/E5E3-8FAE/sample0.mp3");

        // Play internet content
        // playMedia("https://allthingsaudio.wikispaces.com/file/view/Shuffle%20for%20K.M.mp3/139190697/Shuffle%20for%20K.M.mp3");
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
    private void playMedia (String media) {
        // First bind
        if (!bServiceConnected) {
            Intent playerIntent = new Intent (this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
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
            Intent broadcastIntent = new Intent(BC_STOP_AUDIO_WARIH);
            sendBroadcast(broadcastIntent);
        }
    }

    private void pauseMedia(){
        if (bServiceConnected) {
            Intent broadcastIntent = new Intent(BC_PAUSE_AUDIO_WARIH);
            sendBroadcast(broadcastIntent);
        }
    }

    private void resumeMedia(){
        if (bServiceConnected) {
            Intent broadcastIntent = new Intent(BC_RESUME_AUDIO_WARIH);
            sendBroadcast(broadcastIntent);
        }
    }
}
