package org.alpha.conference2013.diary;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReminderReceiver extends BroadcastReceiver {

    private static int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        String title = extras.getString("title");
        String note = extras.getString("note");
        Integer sessionId = extras.getInt("sessionId");
        Integer reminder = extras.getInt("reminder");

        Intent notificationIntent = new Intent(context, ReminderActivity.class);
        notificationIntent.putExtra(ReminderActivity.EXTRA_SESSION_ID, sessionId);
        notificationIntent.putExtra(ReminderActivity.EXTRA_REMINDER, reminder);

        PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, notificationIntent, 0);
        Notification notification = new Notification(android.R.drawable.ic_lock_idle_alarm, "Session Reminder", System.currentTimeMillis());
        notification.setLatestEventInfo(context, title, note, contentIntent);
        notification.flags = Notification.FLAG_INSISTENT;
        notification.defaults |= Notification.DEFAULT_SOUND;

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID++, notification);
    }

};