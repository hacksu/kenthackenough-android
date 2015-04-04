package io.khe.kenthackenough;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import java.util.Date;

class Message implements Comparable<Message> {
    public Date created;
    public String message;
    private static int nextNotificationID;

    public Message(Date created, String message) {
        this.created = created;
        this.message = message;
    }

    public String timeSince() {
        Date now = new Date();
        long deltaMills = now.getTime() - created.getTime();
        long deltaSeconds = deltaMills / 1000;
        long deltaMinutes = deltaSeconds / 60;
        long deltaHours = deltaMinutes / 60;
        long deltaDays = deltaHours / 24;

        String noun;
        long value;

        if (deltaDays > 0) {
            noun = "day";
            value = deltaDays;
        } else if (deltaHours != 0) {
            noun = "hour";
            value = deltaHours;
        } else if (deltaMinutes != 0) {
            noun = "minute";
            value = deltaMinutes;
        } else if (deltaSeconds != 0) {
            noun = "second";
            value = deltaSeconds;
        } else {
            noun = "millisecond";
            value = deltaMills;
        }

        if (value == 1) {
            return "About a" + (noun.charAt(0) == 'h' ? "n " : " ") + noun + " ago";
        } else {
            return Long.toString(value) + " " + noun + "s ago";
        }
    }

    @Override
    public int hashCode() {
        return message.hashCode() ^ created.hashCode(); //not sure if this is best
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Message)) {
            return super.equals(other);
        }

        Message otherMessage = (Message) other;
        return this.message.equals(otherMessage.message) && this.created.equals(otherMessage.created);

    }

    @Override
    public int compareTo(Message another) {
        return this.created.compareTo(another.created) * -1;
    }

    public void notify(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.clover);
        builder.setContentTitle("Update from KHE");
        builder.setContentText(Html.fromHtml(message));
        builder.setAutoCancel(true);
        builder.setCategory("CATEGORY_MESSAGE");
        builder.setPriority(1);
        builder.setDefaults(Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);
        builder.setWhen(created.getTime());

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("view", 1);



        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("messages", nextNotificationID++, builder.build());
    }


}