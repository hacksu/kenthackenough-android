package io.khe.kenthackenough.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;

import com.github.rjeschke.txtmark.Processor;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.khe.kenthackenough.MainActivity;
import io.khe.kenthackenough.R;

public class Message implements Comparable<Message> {
    private Date created;
    private String message;
    private SpannableString formatted;
    private static int nextNotificationID;
    private Long[] id;
    private int notificationId = -1;
    private static Map<List<Long>, Message> idToMessage = new HashMap<>();

    public Message(Date created, String message, Long[] id) {
        this.created = created;
        this.message = message;

        // create a formatted string from the html and strip bad links
        formatted = new SpannableString(Html.fromHtml(message));
        URLSpan[] links = formatted.getSpans(0,formatted.length(), URLSpan.class);
        this.id = id;
        idToMessage.put(Arrays.asList(id), this);

        for (URLSpan link : links) {

            // remove and re-add if it's valid
            int start = formatted.getSpanStart(link);
            int end = formatted.getSpanEnd(link);
            int flags = formatted.getSpanFlags(link);
            formatted.removeSpan(link);
            try {
                URL url = new URL(link.getURL());
                link = new URLSpan(url.toString());
                formatted.setSpan(link, start, end, flags);
            } catch (MalformedURLException e) {
                try {
                    // make an attempt to fix simple cases of missing http://
                    URL url = new URL("http://" + link.getURL());
                    link = new URLSpan(url.toString());
                    formatted.setSpan(link, start, end, flags);
                } catch (MalformedURLException e1) {
                    Log.e("KHE2015", "Bad URL: " +link.getURL()+ " in " + message);
                }
            }
        }
    }

    public static Message getFromJSON(JSONObject json) throws JSONException {
        String htmlMessage = Processor.process(json.getString("text"));
        String uuidString = json.getString("_id");
        Long[] id = new Long[2];
        id[0] = Long.decode('#' + uuidString.substring(0, 12));
        id[1] = Long.decode('#' + uuidString.substring(12));
        return new Message(new DateTime(json.getString("created")).toDate(), htmlMessage, id);
    }

    public static Message getByID(Long[] id) {
        return idToMessage.get(Arrays.asList(id));
    }

    public Date getCreated() {
        return created;
    }

    public String getMessage() {
        return message;
    }

    public Spanned getFormatted() {
        return formatted;
    }


    @Override
    public int hashCode() {
        return id[1].intValue(); // todo better hash
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Message)) {
            return super.equals(other);
        }

        Message otherMessage = (Message) other;
        return this.id[0].equals(otherMessage.id[0]) && this.id[1].equals(otherMessage.id[1]);

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

        notificationId = nextNotificationID++;
        notificationManager.notify("messages", notificationId, builder.build());
    }

    public void closeNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel("messages" , notificationId);
        System.out.println("was found for id:" +idToMessage.get(Arrays.asList(id)).message);



        notificationId = -1;
    }


}