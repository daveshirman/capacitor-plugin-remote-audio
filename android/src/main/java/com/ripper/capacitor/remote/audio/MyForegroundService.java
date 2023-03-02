package com.ripper.capacitor.remote.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MyForegroundService extends Service {

    private static final String PluginTag = "RemoteAudioService";
    private static final Integer NOTIFICATION__ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "com.ripper.capacitor.remote.audio";
    private static final String NOTIFICATION_CHANNEL_NAME = "AOM Player Service";
    private static final String NOTIFICATION_CHANNEL_DESCRIPTION = "For playback controls for the AOM app";

    public static final String EXTRA_Top_Level_Package_Name = "EXTRA_Top_Level_Package_Name";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE_FROM_NOTIFICATION = "ACTION_PAUSE_FROM_NOTIFICATION";
    public static final String ACTION_PLAY_FROM_NOTIFICATION = "ACTION_PLAY_FROM_NOTIFICATION";
    public static final String ACTION_RW = "ACTION_RW";
    public static final String ACTION_FF = "ACTION_FF";
    public static final String ACTION_SEEK = "ACTION_SEEK";
    public static final String ACTION_GET_CURRENT_POSITION = "ACTION_GET_CURRENT_POSITION";

    private static final int skipMilliseconds = 10000;
    private boolean playerDidLoad = false;
    private boolean playerDidFinish = false;
    private String topLevelPackageName = "";
    private String url;
    private String title;

    private SimpleExoPlayer mp;


    public MyForegroundService() {

    }

    private int getNotificationIcon() {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("notificationlogo", "drawable",
                this.getPackageName());
        return resourceId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(PluginTag, "My foreground service onCreate().");

        // Note: we are showing this 'Loading...' notification here, then updating it after so that the app doesn't crash.
        // This is because Android requires the notification to be shown within 5s of starting the service.

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setShowWhen(false)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle("AOM Player Loading...");

        startForeground(NOTIFICATION__ID, builder.build());
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null)
        {
            String action = intent.getAction();

            switch (action)
            {
                case ACTION_START:
                    topLevelPackageName = intent.getExtras().get(EXTRA_Top_Level_Package_Name).toString();
                    title = intent.getExtras().get("title").toString();
                    url = intent.getExtras().get("url").toString();
                    setupPlayerNotification();
                    break;
                case ACTION_STOP:
                    stopService();
                    break;
                case ACTION_PLAY:
                    play(false);
                    break;
                case ACTION_PAUSE:
                    pause(false);
                    break;
                case ACTION_PLAY_FROM_NOTIFICATION:
                    play(true);
                    break;
                case ACTION_PAUSE_FROM_NOTIFICATION:
                    pause(true);
                    break;
                case ACTION_SEEK:
                    int milliseconds = (int)intent.getExtras().get("milliseconds");
                    seek(milliseconds);
                    break;
                case ACTION_GET_CURRENT_POSITION:
                    getCurrentPosition();
                    break;
                case ACTION_RW:
                    long position1 = mp.getCurrentPosition() - skipMilliseconds;
                    seek(position1);
                    break;
                case ACTION_FF:
                    long position2 = mp.getCurrentPosition() + skipMilliseconds;
                    seek(position2);
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void setupPlayerNotification()
    {
        Log.i(PluginTag, "setupPlayerNotification");
        updateNotification();
        getMediaInfo();
    }

    private NotificationCompat.Builder buildPlayerNotification() {
        // Create notification default intent to open the MainActivity from Capacitor app when tapped.
        Intent intent = getPackageManager().getLaunchIntentForPackage(topLevelPackageName);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setShowWhen(false)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(title)
                .setContentText("Loading...")
                .setContentIntent(pendingIntent);

        if (playerDidFinish) {
            builder.setContentText("Playback finished 👍");
        }

        // Note: https://stackoverflow.com/questions/44698440/android-26-o-notification-doesnt-display-action-icon
        // Note: Icons don't show any more.
        // Only want to see these buttons if we've loaded the audio file without error.
        if (!playerDidFinish && playerDidLoad) {
            builder.setContentText(null);

            // Add rewind 15s
            Intent rwIntent = new Intent(this, MyForegroundService.class);
            rwIntent.setAction(ACTION_RW);
            PendingIntent pendingRWIntent = PendingIntent.getService(this, 0, rwIntent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Action rwAction = new NotificationCompat.Action(android.R.drawable.ic_media_rew, "⏪ 10s", pendingRWIntent);
            builder.addAction(rwAction);

            if (mp != null && mp.isPlaying()) {
                // Add Pause
                Intent pauseIntent = new Intent(this, MyForegroundService.class);
                pauseIntent.setAction(ACTION_PAUSE_FROM_NOTIFICATION);
                PendingIntent pendingPauseIntent = PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Action pauseAction = new NotificationCompat.Action(android.R.drawable.ic_media_pause, "⏸️ Pause", pendingPauseIntent);
                builder.addAction(pauseAction);
            }
            else if (mp == null || mp != null && !mp.isPlaying()) {
                // Add Play
                Intent playIntent = new Intent(this, MyForegroundService.class);
                playIntent.setAction(ACTION_PLAY_FROM_NOTIFICATION);
                PendingIntent pendingPlayIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_IMMUTABLE);
                NotificationCompat.Action playAction = new NotificationCompat.Action(android.R.drawable.ic_media_play, "▶️ Play", pendingPlayIntent);
                builder.addAction(playAction);
            }

            // Add fast-forward 15s button
            Intent ffIntent = new Intent(this, MyForegroundService.class);
            ffIntent.setAction(ACTION_FF);
            PendingIntent pendingFFIntent = PendingIntent.getService(this, 0, ffIntent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Action ffAction = new NotificationCompat.Action(android.R.drawable.ic_media_ff, "⏩ 10s", pendingFFIntent);
            builder.addAction(ffAction);
        }

        return builder;
    }

    private void updateNotification() {
        NotificationCompat.Builder builder = buildPlayerNotification();
        NotificationManagerCompat.from(this).notify(NOTIFICATION__ID, builder.build());
    }

    private void stopService()
    {
        stopPlayer();
        Log.i(PluginTag, "Stop foreground service.");
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        this.stopService();
    }




    private void getMediaInfo() {
        try {
            playerDidLoad = false;
            playerDidFinish = false;
            mp = new SimpleExoPlayer.Builder(this).build();
            mp.setMediaItem(MediaItem.fromUri(url));
            mp.prepare();
            mp.addListener(new Player.EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (playbackState == ExoPlayer.STATE_READY) {
                        long duration = getDuration();
                        if (duration > 0) {
                            Intent intent = new Intent(SharedConstants.GetMediaInfo);
                            intent.putExtra("result", duration);
                            playerDidLoad = true;
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                            updateNotification();
                        }
                        else {
                            Intent intent = new Intent(SharedConstants.GetMediaInfo);
                            intent.putExtra("result", "");
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        }
                    }

                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        playerDidFinish = true;
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    Intent intent = new Intent(SharedConstants.GetMediaInfo);
                    intent.putExtra("result", "");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            });


        }
        catch (Exception e) {
            Intent intent = new Intent(SharedConstants.GetMediaInfo);
            intent.putExtra("result", "");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private long getDuration() {
        try {
            return mp.getDuration();
        }
        catch (Exception ex) {
            return -1;
        }
    }

    private void stopPlayer() {
        Log.i(PluginTag, "stop");
        try {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.release();
        }
        catch (Exception ex) {
            Log.i(PluginTag, "stop failed");
        }
    }

    private void play(boolean wasFromNotification) {
        try {
            mp.play();
            updateNotification();
            Intent intent = new Intent(SharedConstants.Play);
            intent.putExtra("result", mp.isPlaying());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            if (wasFromNotification) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                        new Intent(SharedConstants.UpdateAppUI_WasPlayed)
                );
            }
        }
        catch (Exception e) {
            Intent intent = new Intent(SharedConstants.Play);
            intent.putExtra("result", false);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void pause(boolean wasFromNotification) {
        if (mp.isPlaying()) {
            Log.i(PluginTag, "pause");
            mp.pause();
            updateNotification();

            if (wasFromNotification) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                        new Intent(SharedConstants.UpdateAppUI_WasPaused)
                );
            }
        }
    }

    private void seek(long milliseconds) {
        Log.i(PluginTag, "seek");
        try {
            mp.seekTo(milliseconds);
        }
        catch (Exception ex) {
            Log.i(PluginTag, "seek failed");
        }
    }

    private void getCurrentPosition() {
        Log.i(PluginTag, "getCurrentPosition");
        long position = -1;

        if (playerDidFinish) {
            position = mp.getDuration();

            updateNotification();
        }
        else {
            try {
                position = mp.getCurrentPosition();
            }
            catch (Exception ex) {
                Log.i(PluginTag, "getCurrentPosition failed");
            }
        }

        Intent intent = new Intent(SharedConstants.GetCurrentPosition);
        intent.putExtra("result", position);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}