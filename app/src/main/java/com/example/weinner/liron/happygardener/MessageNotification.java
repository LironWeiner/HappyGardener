package com.example.weinner.liron.happygardener;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Helper class for showing and canceling message
 * notifications.
 * <p>
 * This class makes heavy use of the {@link Notification} helper
 * class to create notifications in a backward-compatible way.
 */
class MessageNotification {
    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     * <p>
     * TODO: Customize this method's arguments to present relevant content in
     * the notification.
     * <p>
     * TODO: Customize the contents of this method to tweak the behavior and
     * presentation of message notifications. Make
     * sure to follow the
     * <a href="https://developer.android.com/design/patterns/notifications.html">
     * Notification design guidelines</a> when doing so.
     *
     * @see #cancel(Context , int)
     */
    public void notify(final Context context,
                       final String customer_name , final int number , final int customer_id , final String date , final String repeat) {
        final Resources res = context.getResources();
        // This image is used as the notification's large icon (thumbnail).

        final String text = res.getString(R.string.reminder);

        Intent resultIntent = new Intent(context, UpdateCustomerActivity.class); // When the user touch the notification go to this activity
        resultIntent.putExtra("id" , String.valueOf(customer_id));

        int color = Color.parseColor("#26d28d"); // app green
        //int color = Color.argb(196 , 233 ,5 , 5);

        //Notification.Builder builder = new Notification.Builder(context)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context , TimeAlarm.CHANNEL_ID)
                //NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)
                .setColor(color)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(text)
                .setContentText(customer_name)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTicker(customer_name)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setNumber(number)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context,
                                customer_id,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT));
        // Automatically dismiss the notification when it is touched.
        //.setAutoCancel(true);
        Notification notification = builder.build();
        Intent notificationIntent = new Intent(context , TimeAlarm.class);
        notificationIntent.putExtra("id",customer_id);///
        notificationIntent.putExtra(TimeAlarm.NOTIFICATION_ID, customer_id);
        notificationIntent.putExtra(TimeAlarm.NOTIFICATION , notification);

        // Enable a receiver TimeAlarm
        ComponentName receiverTimeAlarm = new ComponentName(context, TimeAlarm.class);
        PackageManager pmTimeAlarmBoot = context.getPackageManager();

        pmTimeAlarmBoot.setComponentEnabledSetting(receiverTimeAlarm,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        //


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context , customer_id , notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        //alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
        long the_date = getDate(date);

        if(R.id.radioButton_none == Integer.valueOf(repeat)){
            //Log.d("mytest", "notify: none repeat");
            alarmManager.setExact(AlarmManager.RTC, the_date, pendingIntent);
        }
        else if(R.id.radioButton_weekly == Integer.valueOf(repeat)){
            //Log.d("mytest", "notify: weekly repeat");
            alarmManager.setRepeating(AlarmManager.RTC , the_date , AlarmManager.INTERVAL_DAY * 7 , pendingIntent);
        }
        else if(R.id.radioButton_monthly == Integer.valueOf(repeat)){
            //Log.d("mytest", "notify:  monthly");
            alarmManager.setRepeating(AlarmManager.RTC , the_date , AlarmManager.INTERVAL_DAY * 30 , pendingIntent);
        }
        else if(R.id.radioButton_twice_weekly == Integer.valueOf(repeat)){
            //Log.d("mytest", "notify:  twice_weekly");
            alarmManager.setRepeating(AlarmManager.RTC , the_date , AlarmManager.INTERVAL_DAY * 14 , pendingIntent);
        }

    }

    //Cancels any notification's alarm  of this type previously shown using
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public void cancel(final Context context , final int id) {
        //Log.d("mytest", "cancel:");
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(id);

        //DBHelper.getInstance(context).deleteNotification(id);
        // If no notifications disable TimeAlarm
        if(DBHelper.getInstance(context).isNotificationsEmpty()) {
            //Log.d("mytest", "cancel: alarm service");
            // Disable a receiver TimeAlarm
            ComponentName receiverTimeAlarm = new ComponentName(context, TimeAlarm.class);
            PackageManager pmTimeAlarmBoot = context.getPackageManager();

            pmTimeAlarmBoot.setComponentEnabledSetting(receiverTimeAlarm,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            //
        }

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TimeAlarm.class);    //Time alarm is the broadcast receiver you use for alarm
        PendingIntent sender = PendingIntent.getBroadcast(context,id, intent, 0);
        am.cancel(sender);
    }

    private long getDate(String date) {
        boolean hour = false;

        GregorianCalendar current = new GregorianCalendar();
        current.setTimeInMillis(System.currentTimeMillis());

        Calendar notification_calendar = Calendar.getInstance(); // The user notification's date
        notification_calendar.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat curDateFormat ;
        if(date.contains(" ")){
            curDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            hour = true;
        }
        else{
            curDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }

        try {
            Date current_date = curDateFormat.parse(date);
            current.setTime(current_date);

            // you can use this date string now
        } catch (Exception e) {
            e.printStackTrace();
        }

        // If the user didn't entered hours and minutes set to 9:30 at morning
        if(!hour){
            current.add(Calendar.HOUR_OF_DAY, 9);
            current.add(Calendar.MINUTE, 30);
            current.add(Calendar.SECOND, 0);
        }

        return current.getTimeInMillis();
    }
}
