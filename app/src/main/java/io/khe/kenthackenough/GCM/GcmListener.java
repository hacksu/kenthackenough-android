package io.khe.kenthackenough.GCM;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import io.khe.kenthackenough.Config;
import io.khe.kenthackenough.R;

/**
 * GcmListener is a service which will receive messaging from the backend through GCM messaging
 */
public class GcmListener extends GcmListenerService {
    private static volatile int notificationId = 0;
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(Config.DEBUG_TAG, "got " + data);
        displayNotification(data, this);
    }

    private static void displayNotification(Bundle data, Context context) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.clover);
        notificationBuilder.setContentTitle(data.getString("gcm.notification.title"));
        notificationBuilder.setContentText(Html.fromHtml(data.getString("gcm.notification.body")));
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setCategory("CATEGORY_MESSAGE");
        notificationBuilder.setPriority(1);
        notificationBuilder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);


        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(data.getString("collapse_key"), notificationId++, notificationBuilder.build());
    }

}
