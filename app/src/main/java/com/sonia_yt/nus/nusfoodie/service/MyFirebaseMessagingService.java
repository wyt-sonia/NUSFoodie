package com.sonia_yt.nus.nusfoodie.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sonia_yt.nus.nusfoodie.MainActivity;
import com.sonia_yt.nus.nusfoodie.OrderRecordActivity;
import com.sonia_yt.nus.nusfoodie.R;
import com.sonia_yt.nus.nusfoodie.model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final String ADMIN_CHANNEL_ID ="admin_channel";
    private final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String body = "", title = "";

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
             body = remoteMessage.getNotification().getBody();
             title = remoteMessage.getNotification().getTitle();
        }
        sendNotification(title, body, remoteMessage.getData());
        OrderRecordActivity.update = true;
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("NEW_TOKEN",s);
    }


    private void sendNotification(String title, String messageBody, Map<String, String> data) {

        String stallName = "", itemInfo = "", collectionTime = "", cusID = "";

        Intent intent_appOff = new Intent(this, MainActivity.class);
        Intent intent_appOn = new Intent(this, OrderRecordActivity.class);
        intent_appOff.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent_appOn.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = null;

        if (data != null) {
            stallName = data.get("stallName");
            itemInfo = data.get("itemInfo");
            collectionTime = data.get("collectTime");
            cusID = data.get("cusID");
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.dondon);

        String channelId = ADMIN_CHANNEL_ID;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        try{
            Date temp = sdf.parse(collectionTime);
            Calendar tempCal = Calendar.getInstance();
            tempCal.setTime(temp);
            collectionTime = "" + tempCal.HOUR_OF_DAY + ":" + tempCal.MINUTE + " " + tempCal.DAY_OF_MONTH + "/" + tempCal.MONTH + "/" + tempCal.YEAR;
        } catch (Exception ex) {

        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.dondon)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.InboxStyle()
                                .addLine("Collection Time: " + collectionTime)
                                .addLine("Stall Name: " + stallName)
                                .addLine(itemInfo)
                                .addLine("Order by: " + cusID))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri);

        if(OrderRecordActivity.order_running) {

        } else {
            if (User.currUser != null)
                pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent_appOn,
                    PendingIntent.FLAG_ONE_SHOT);
            else
                pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent_appOff,
                        PendingIntent.FLAG_ONE_SHOT);

            notificationBuilder.setContentIntent(pendingIntent);
        }


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setColor(Color.parseColor("#FF5722"));
            NotificationChannel channel = new NotificationChannel(channelId,
                    "New notification from NUSFoodie",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to devie notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(getResources().getColor(R.color.colorPrimary));
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
