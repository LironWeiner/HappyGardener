package com.example.weinner.liron.happygardener;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;

public class TimeAlarm extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String CHANNEL_ID = "channel-01";
    public static String CHANNEL_NAME = "channel name";


    public void onReceive(Context context, Intent intent) {
        //Log.d("boottest", "onReceive TimeAlarm:");
        // If phone boots then reset all the alarms and notifications
        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                // Set the alarm here.
                //Log.d("boottest", "onReceive TimeAlarmBoot: BOOT_COMPLETED");
                ArrayList<com.example.weinner.liron.happygardener.Notification> notificationsList = DBHelper.getInstance(context).getAllNotifications();
                for (com.example.weinner.liron.happygardener.Notification item : notificationsList) {
                    new MessageNotification().notify(context, item.getCustomer_name(), 5,
                            item.getCustomer_id(),
                            item.getNotification_date(),
                            item.getRepeat());
                }
            }
        }
        else {
            //Log.d("boottest", "onReceive: NOT A BOOT_COMPLETE");
            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(id , notification);
        }
    }
}
