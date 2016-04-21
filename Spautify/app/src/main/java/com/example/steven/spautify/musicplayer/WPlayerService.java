package com.example.steven.spautify.musicplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.IBinder;
import android.util.Log;

import com.example.Notifier;
import com.example.steven.spautify.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Steven on 1/21/2016.
 *
 * Tutorial: https://www.binpress.com/tutorial/using-android-media-style-notifications-with-media-session-controls/165
 *
 * This class requires API 21 to work
 *
 * Don't forget to register this in the manifest, the tutorial above decided not to mention that...
 *
 * "the service remains running until it stops itself with stopSelf() or another component stops it by calling stopService()."
 */
@SuppressLint("NewApi")
public class WPlayerService extends Service {
    private static final String TAG = "WPlayerService";


    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_REWIND = "action_rewind";
    public static final String ACTION_FAST_FORWARD = "action_fast_foward";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";

    //private MediaPlayer mMediaPlayer;
    private MediaSessionManager mManager;
    private MediaSession mSession;
    private MediaController mController;

    private ScheduledFuture mHeartbeat;
    private Notifier.Listener<WPlayer.Notif> mListener;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void initMediaSessions() {
        Log.e(TAG, "initMediaSessions");
        //mMediaPlayer = new MediaPlayer();
        // Create media com.example.steven.spautify.player here???

        // TODO register listeners that call buildNotification(generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE)); when changed external of here??

        mManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        //mSession = mManager.createSession("sample session");
        //mController = MediaController.fromToken( mSession.getSessionToken() );
        mSession = new MediaSession(getApplicationContext(), "simple session");
        mController = new MediaController(getApplicationContext(), mSession.getSessionToken());

        mSession.setCallback(
                new MediaSession.Callback(){
             @Override
             public void onPlay() {
                 super.onPlay();
                 Log.e(TAG, "onPlay ");

                 if (checkHearbeat()) {
                     WPlayer.playpause();
                     buildNotification();
                 }

             }

             @Override
             public void onPause() {
                 super.onPause();
                 Log.e(TAG, "onPause ");
                 if (checkHearbeat()) {
                     WPlayer.playpause();
                     buildNotification();
                 }
             }

             @Override
             public void onSkipToNext() {
                 super.onSkipToNext();
                 Log.e(TAG, "onSkipToNext ");
                 if (checkHearbeat()) {
                     WPlayer.skipToNext();
                     buildNotification();
                 }
             }

             @Override
             public void onSkipToPrevious() {
                 super.onSkipToPrevious();
                 Log.e(TAG, "onSkipToPrevious");
                 if (checkHearbeat()) {
                     WPlayer.skipToPrevious();
                     buildNotification();
                 }
             }


             @Override
             public void onStop() {
                 super.onStop();
                 Log.e(TAG, "onStop");
                 kill();
             }

             @Override
             public void onSeekTo(long pos) {
                 super.onSeekTo(pos);
             }

             @Override
             public void onSetRating(Rating rating) {
                 super.onSetRating(rating);
             }
         }
        );



        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);



        checkHearbeat();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Called whenever an intent is received; buttons that the notif bar displays also cause these intents.
        Log.d(TAG, "onStartCommand");
        if (mManager == null ) {
            initMediaSessions();
        }

        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public void onDestroy() {
        Log.w(TAG, "onDestroy");
        mSession.release();
        if (mHeartbeat != null) {
            mHeartbeat.cancel(false);
            mHeartbeat = null;
        }


        WPlayer.getNotifier().unregisterListener(mListener);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);


        super.onDestroy();
    }

    private void kill() {
        stopSelf();
        //Our own way to trigger death of this service
        //Stop media player here

//        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancel(1);
//        Intent intent = new Intent( getApplicationContext(), WPlayerService.class );
//        stopService(intent);

    }


    private void handleIntent( Intent intent ) {

        if( intent == null || intent.getAction() == null )
            return;
        String action = intent.getAction();
        Log.i(TAG, "handleIntent " + action);


        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            mController.getTransportControls().play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            mController.getTransportControls().pause();
        } else if( action.equalsIgnoreCase( ACTION_FAST_FORWARD ) ) {
            mController.getTransportControls().fastForward();
        } else if( action.equalsIgnoreCase( ACTION_REWIND ) ) {
            mController.getTransportControls().rewind();
        } else if( action.equalsIgnoreCase( ACTION_PREVIOUS ) ) {
            mController.getTransportControls().skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            mController.getTransportControls().skipToNext();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
            mController.getTransportControls().stop();
        }
    }




    private Notification.Action generateAction( int icon, String title, String intentAction ) {
        //Log.e(TAG, "generateAction");
        Intent intent = new Intent( getApplicationContext(), WPlayerService.class );
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder( icon, title, pendingIntent ).build();
    }


    //private void buildNotification( Notification.Action action ) {
    private void buildNotification() {
        //Log.i(TAG, "buildNotification");

        if (checkHearbeat()) {
            Notification.Action action = null;
            if (WPlayer.getPlaybackState()== WPlayer.PlaybackState.NotPlaying) {
                action = generateAction(android.R.drawable.ic_media_play, "Play", ACTION_PLAY);
            } else if (WPlayer.getPlaybackState()== WPlayer.PlaybackState.Playing) {
                action = generateAction(android.R.drawable.ic_media_pause, "Pause", ACTION_PAUSE);
            }

            String title = "no song";
            String artist = "";
            // TODO service shouldn't exist without a song loaded?
            if (WPlayer.getCurrentSng() != null) {
                title = WPlayer.getCurrentSng().name;
                artist = WPlayer.getCurrentSng().artistPrimaryName;
            }


            Notification.MediaStyle style = new Notification.MediaStyle();

            Intent intent = new Intent(getApplicationContext(), WPlayerService.class);
            intent.setAction(ACTION_STOP);
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
            Notification.Builder builder = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_action_play)
                    .setContentTitle(title)
                    .setContentText(artist)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)

                    .setDeleteIntent(pendingIntent)
                    .setAutoCancel(false)
                    .setOngoing(true) // removes ability to swipe or clear
                    .setShowWhen(false) // dont show notif timestamp
                    .setStyle(style);

            //builder.addAction(generateAction(android.R.drawable.ic_media_previous, "Previous", ACTION_PREVIOUS));
            //builder.addAction( generateAction( android.R.drawable.ic_media_rew, "Rewind", ACTION_REWIND ) );
            if (action != null) builder.addAction(action);
            //builder.addAction( generateAction( android.R.drawable.ic_media_ff, "Fast Forward", ACTION_FAST_FORWARD ) );
            builder.addAction(generateAction(android.R.drawable.ic_media_next, "Next", ACTION_NEXT));
            if (action != null) {
                style.setShowActionsInCompactView(0, 1); // 2, 3, 4);
            } else {
                style.setShowActionsInCompactView(0); // 2, 3, 4);
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, builder.build());

        } else {

            // So service shouldn't exist when no player ready
            stopSelf();

        }
    }

    private boolean checkHearbeat() {
        //Log.i(TAG, "checkHeartbeat ");
        if (WPlayer.getState() != WPlayer.WPlayerState.Off) {
            if (mHeartbeat == null) {
                ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                mHeartbeat = service.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        checkHearbeat();
                    }
                }, 2, 2, TimeUnit.SECONDS);
            }

            if (mListener == null) {
                mListener = new Notifier.Listener<WPlayer.Notif>() {
                    @Override
                    public void onChange(WPlayer.Notif type) {
                        switch (type) {
                            case PlaybackAndQueue:
                            case Playback:
                                buildNotification();
                                // TODO make more efficient since we dont care for just position changes


                                break;

                        }

                    }
                };

                WPlayer.getNotifier().registerListener(mListener);
            }



            return true;
        } else {
            Log.e(TAG, "Player is off!  Stopping WPlayerService");

            kill();
            return false;
        }

    }




}
