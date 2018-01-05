package com.example.ccsph2.mediaplayerdemo;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
        prepareContentList();
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

    public void onClickPlayButton(View view){

        // For debug check external storage path
        // File file = null;
        // file = Environment.getExternalStorageDirectory();
        // Log.v("WarihDebug", "root="+file.getAbsolutePath());

        // Play SD card content
        // playMedia("/storage/emulated/0/sample0.mp3");
        // playMedia("/storage/E5E3-8FAE/sample0.mp3");

        // Play internet content
        playMedia("https://allthingsaudio.wikispaces.com/file/view/Shuffle%20for%20K.M.mp3/139190697/Shuffle%20for%20K.M.mp3");
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
    private void playMedia (String media) {
        ContentDataStorage storage = new ContentDataStorage(getApplicationContext());
        // temporary
        int contentIndex = 0;

        // First bind
        if (!bServiceConnected) {
            // Store Content List to Shared Preferences
            storage.storeContent(contentList);
            storage.storeContentIndex(contentIndex);

            // Throw intent
            Intent playerIntent = new Intent (this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        else {
            // Store latest selected content index
            storage.storeContentIndex(contentIndex);

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

    ArrayList<ContentData> contentList = new ArrayList<ContentData>();

    private void prepareContentList() {
        ContentResolver contentResolver = getContentResolver();

        // For External Storage (SD Card, etc)
        // Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // For Internal Storage (good for emulator)
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        // Content provider
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            contentList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                // Save to audioList
                contentList.add(new ContentData(data, title, album, artist, duration));
            }
        }
        cursor.close();
    }
}
