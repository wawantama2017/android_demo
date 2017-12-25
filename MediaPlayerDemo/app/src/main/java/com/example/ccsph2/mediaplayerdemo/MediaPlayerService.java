package com.example.ccsph2.mediaplayerdemo;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;


public class MediaPlayerService extends Service
        implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    /**
     * Global Instances
     */
    private MediaPlayer mMediaPlayer;
    private String mMediaFile; // fixed path to actual media file
    //Used to pause/resume MediaPlayer
    private int mCurrentPosition;

    // Binder for clients
    private final IBinder mIBinder = new LocalBinder();

    // Audio Manager
    private AudioManager mAudioManager;

    private AudioFocusRequest mFocusRequest;

    // Lock
    final Object mLock = new Object();

    /**
     * Service callback methods
     */

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // register broadcast receivers
        register_playAudio();
        register_audioInterrupt(); // for headset plug, unplug, output change
    }

    //The system calls this method when an activity, requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            // Audio file is passed to the service through putExtra()
            mMediaFile = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();
        }

        if (requestAudioFocus() == false) {
            stopSelf();
        }

        if (mMediaFile != null && mMediaFile != "") {
            initializeMediaPlayer();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopMedia();
            mMediaPlayer.release();
        }
        abandonAudioFocus();
    }

    /**
     * MediaPlayer callback methods
     */

    @Override
    public void onCompletion(MediaPlayer mp) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // Invoked when error occurred during async operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.v("MP Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.v("MP Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.v("MP Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.v("MP Error", "MEDIA ERROR IO " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                Log.v("MP Error", "MEDIA ERROR MALFORMED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Log.v("MP Error", "MEDIA ERROR UNSUPPORTED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Log.v("MP Error", "MEDIA ERROR TIMED OUT " + extra);
                break;
            default:
                Log.v("MP Error", "MEDIA ERROR" + extra);
                break;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }


    /**
     * Service Binder
     */
    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }
    }

    /**
     * MediaPlayer actions
     */
    private void initializeMediaPlayer() {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }

        //Set up MediaPlayer event listeners
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        //Reset to avoid MediaPlayer pointing to another data source
        mMediaPlayer.reset();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Set the data source to the mediaFile location
            mMediaPlayer.setDataSource(mMediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        // WARIH DEBUG
        Log.v("WarihDebug", "path="+mMediaFile);
        //
        mMediaPlayer.prepareAsync(); // asynchronously prepare
    }

    private void playMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void stopMedia() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void pauseMedia() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mCurrentPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public void resumeMedia() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mCurrentPosition);
            mCurrentPosition = 0;
            mMediaPlayer.start();
        }
    }

    /**
     * Broadcast Receivers Registration
     */

    private void register_playAudio(){
        IntentFilter filter = new IntentFilter(com.example.ccsph2.mediaplayerdemo.MainActivity.BC_PLAY_AUDIO_WARIH);
        registerReceiver(receivePlayAudio, filter);
    }

    private void register_audioInterrupt(){
        registerReceiver(receiveAudioInterrupt, new IntentFilter(mAudioManager.ACTION_AUDIO_BECOMING_NOISY));
        registerReceiver(receiveAudioInterrupt, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    /**
     * Broadcast Receivers
     */

    private BroadcastReceiver receivePlayAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset to play from the beginning
            stopMedia();
            mMediaPlayer.reset();
            initializeMediaPlayer();
        }
    };

    private BroadcastReceiver receiveAudioInterrupt = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) {
                return;
            }
            switch (action){
                case Intent.ACTION_HEADSET_PLUG:
                    pauseMedia();
                    break;
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    mMediaPlayer.setVolume(0.5f, 0.5f);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Audio Focus Request and Abandon
     */

    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // For API Level 26 and later
        // Special for Android 8.0 and later
        /*private AudioAttributes playbackAttributes;
        playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(AudioManager.OnAudioFocusChangeListener);
        int res = mAudioManager.requestAudioFocus(mFocusRequest);*/

        int res = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (res != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return false;
        }
        return true;
    }

    private void abandonAudioFocus(){
        mAudioManager.abandonAudioFocusRequest(mFocusRequest);
    }

    /**
     * Audio Focus Listener
     */
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN :
                // Used to indicate a gain of audio focus
                // or a request of audio focus, of unknown duration.
                // example: after YouTube App terminated, after phone call
                if (mMediaPlayer == null) {
                    initializeMediaPlayer();
                } else if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(mCurrentPosition);
                    mMediaPlayer.start();
                    mMediaPlayer.setVolume(1.0f, 1.0f); // maximum volume
                } else {}
                break;
            case AudioManager.AUDIOFOCUS_LOSS :
                // Used to indicate a loss of audio focus of unknown duration.
                // example: YouTube
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT :
                // Used to indicate a transient loss of audio focus.
                // example Phone call
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mCurrentPosition = mMediaPlayer.getCurrentPosition();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK :
                // Used to indicate a transient loss of audio focus
                // where the loser of the audio focus can lower its output volume
                // if it wants to continue playing (also referred to as "ducking")
                // as the new focus owner doesn't require others to be silent.
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.setVolume(0.5f, 0.5f);
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
            case AudioManager.AUDIOFOCUS_NONE:
            default:
                break;
        }
    }
}
