package it.unipd.dei.esp1415.thetrumannshow.FallDetector.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import it.unipd.dei.esp1415.thetrumannshow.FallDetector.Activities.RunningSessionActivity;
import it.unipd.dei.esp1415.thetrumannshow.FallDetector.R;

public class PersistentNotificationManager {
    private static final int PERSISTENT_NOTIFICATION_ID = 1;

    public static void createPersistentNotificationForRunningSession(Context appContext) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(appContext)
                        .setSmallIcon(R.mipmap.notification_icon)
                        .setContentTitle(appContext.getString(R.string.notification_title_text))
                        .setContentText(appContext.getString(R.string
                                .notification_content_text))
                        .setOngoing(true);

        Intent resultIntent = new Intent(appContext, RunningSessionActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
        stackBuilder.addParentStack(RunningSessionActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context
                .NOTIFICATION_SERVICE);
        mNotificationManager.notify(PERSISTENT_NOTIFICATION_ID, mBuilder.build());
    }

    static void createPersistentNotificationForPausedSession(Context appContext) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(appContext)
                        .setSmallIcon(R.mipmap.notification_icon)
                        .setContentTitle(appContext.getString(R.string.notification_title_text_paused))
                        .setContentText(appContext.getString(R.string
                                .notification_content_text_paused))
                        .setOngoing(true);

        Intent resultIntent = new Intent(appContext, RunningSessionActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
        stackBuilder.addParentStack(RunningSessionActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context
                .NOTIFICATION_SERVICE);
        mNotificationManager.notify(PERSISTENT_NOTIFICATION_ID, mBuilder.build());
    }

    static void deletePersistentNotification(Context appContext) {
        NotificationManager mNotificationManager = (NotificationManager) appContext.getSystemService(Context
                .NOTIFICATION_SERVICE);
        mNotificationManager.cancel(PERSISTENT_NOTIFICATION_ID);
    }
}
