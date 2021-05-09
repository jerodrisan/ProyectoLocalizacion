package com.jesusrodri.localizacionproyecto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


public class MyServiceRadarSound extends Service{


    private MediaPlayer player;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        player = MediaPlayer.create(this, R.raw.sound_6); //select music file
    }

    @Override
    public void onDestroy() {
        player.stop();
        player.release();
        stopSelf();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setNotification();
        player.start();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        stopForeground(true);
                        stopSelf();
                    }
                },
        player.getDuration()+200);


        return Service.START_NOT_STICKY;
    }

    private static final String ANDROID_CHANNEL_ID = "notification_localizacloud_ID_2";// The id of the channel.
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(){

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "radars_channel";
            String description = "radars notificación";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ANDROID_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void setNotification(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
        Intent notifyIntent = new Intent(this, Main2Activity.class);

        notifyIntent.putExtra("tap_action","tap_accion");
        // Set the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setContentTitle("Radar encontrado")
                .setSmallIcon(R.drawable.radar)
                .setContentText("Aviso Radar")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Set the intent that will fire when the user taps the notification:
        //.setContentIntent(notifyPendingIntent) //No funciona en esta app ya que es complicado hacerlo con los fragments
        //.setAutoCancel(true);

        Notification notification = builder.build();
        //notification.flags=Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);


    }


}

