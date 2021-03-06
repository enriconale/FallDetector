package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Receivers;

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

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.MainActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.SettingsActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils.SessionsLab;

/**
 * @author Enrico Naletto
 *         Receiver that displays a simple notification after the phone boot process.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String APP_PACKAGE_NAME = "it.unipd.dei.esp1415.thetrumannshow.FallDetector";
    private static final String LAST_NOTIFICATION_DATE_KEY = "it.unipd.dei.esp1415.thetrumannshow" +
            ".FallDetector.lastNotificationDate";
    private static final long TWELVE_HOURS = 43200000;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences
                (context);
        boolean userWantsBootNotification = sharedPrefs.getBoolean(SettingsActivity
                .PREF_DAILY_NOTIFICATION, false);

        SharedPreferences prefs = context.getSharedPreferences(
                APP_PACKAGE_NAME, Context.MODE_PRIVATE);
        long lastNotification = prefs.getLong(LAST_NOTIFICATION_DATE_KEY, -1L);
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
                    (NotificationManager) context.getSystemService(Context
                            .NOTIFICATION_SERVICE);
            mNotificationManager.notify(2, mBuilder.build());

            prefs.edit().putLong(LAST_NOTIFICATION_DATE_KEY, today).apply();
        }
    }
}
