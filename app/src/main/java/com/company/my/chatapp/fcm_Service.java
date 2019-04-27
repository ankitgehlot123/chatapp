package com.company.my.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Base64;
import android.util.Log;

import com.company.my.chatapp.utils.Session;
import com.company.my.chatapp.utils.utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;
import static android.support.constraint.Constraints.TAG;

public class fcm_Service extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "Chat";
    private String notif_msg=null;
    private Session session=new Session(this);
    private static final String CHANNEL_DESC = "chatapp notification";
    private String type;
    private String username;
    private Map<String, String> data;
    private String mob_no;

    public fcm_Service() {
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e("NEW_TOKEN", s);
        session.setRegisToken(s);
    }



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
             type=remoteMessage.getData().get("type");
             username=remoteMessage.getData().get("username");
             mob_no=remoteMessage.getData().get("mob_no");
             data=remoteMessage.getData();
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e( "Message Notification " ,remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private void sendNotification(RemoteMessage.Notification notification) {
        if(type.equals("text"))
            notif_msg=username+" sent you a message";
        else
            notif_msg=username+" sent you a photo";
        Intent intent = new Intent(this, chat_base.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("is_push",true);
        intent.putExtra("data",(new JSONObject(data)).toString());
        intent.putExtra("username",username);
        intent.putExtra("mob_no",mob_no);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("New message")
                        .setContentText(notif_msg)
                        .setAutoCancel(true)
                        .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_MAX);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

}


