package io.khe.kenthackenough.backend.Messages;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;

import com.github.rjeschke.txtmark.Processor;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.khe.kenthackenough.MainActivity;
import io.khe.kenthackenough.R;
import io.khe.kenthackenough.backend.Utilities;

public class Message implements Comparable<Message> {
    private Date created;
    private String message;
    private SpannableString formatted;
    private static int nextNotificationID;
    private String id; // UUID actually hex, but probably not worth storing as such
    private int notificationId = -1;
    private static Map<String, Message> idToMessage = new HashMap<>();

    public Message(Date created, String message, String id) {
        this.created = created;
        this.message = message;

        // create a formatted string from the html and strip bad links
        formatted = Utilities.getSpannableFromHTML(message);

        this.id = id;
        idToMessage.put(id, this);
    }

    public static Message getFromJSON(JSONObject json) throws JSONException {
        String htmlMessage = Processor.process(json.getString("text"));
        String uuidString = json.getString("_id");

        return new Message(new DateTime(json.getString("created")).toDate(), htmlMessage, uuidString);
    }

    public static Message getByID(String id) {
        return idToMessage.get(id);
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
        return id.hashCode(); // todo better hash
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Message)) {
            return super.equals(other);
        }

        return id.equals(((Message) other).id);
    }

    @Override
    public int compareTo(@NonNull Message another) {
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
        resultIntent.putExtra("view", 2);



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
        System.out.println("was found for id:" +idToMessage.get(id).message);



        notificationId = -1;
    }


}