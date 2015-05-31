package it.unipd.dei.esp1415.thetrumannshow.FallDetector;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Date;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private final String APP_PACKAGE_NAME = "it.unipd.dei.esp1415.thetrumannshow.FallDetector";
    private final String LAST_NOTIFICATION_DATE_KEY = "it.unipd.dei.esp1415.thetrumannshow" +
            ".FallDetector.lastNotificationDate";
    private final long TWELVE_HOURS = 43200000;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences
                (context);
        boolean userWantsBootNotification = sharedPrefs.getBoolean(SettingsActivity
                .PREF_DAILY_NOTIFICATION, false);

        SharedPreferences prefs = context.getSharedPreferences(
                APP_PACKAGE_NAME, Context.MODE_PRIVATE);
        long lastNotification = prefs.getLong(LAST_NOTIFICATION_DATE_KEY, new Date().getTime());
        long today = new Date().getTime();

        if (intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")
                && userWantsBootNotification && (today - lastNotification > TWELVE_HOURS)) {

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.notification_icon)
                            .setContentTitle(context.getString(R.string.boot_notification_title))
                            .setContentText(context.getString(R.string.boot_notification_text))
                            .setAutoCancel(true);

            Intent resultIntent = new Intent(context, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    SessionsLab.get(context).getNotificationManager();
            mNotificationManager.notify(2, mBuilder.build());

            prefs.edit().putLong(LAST_NOTIFICATION_DATE_KEY, today).apply();
        }
    }
}
