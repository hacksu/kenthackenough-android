package io.khe.kenthackenough.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.khe.kenthackenough.EventNotificationPoster;
import io.khe.kenthackenough.KHEApp;
import io.khe.kenthackenough.MainActivity;
import io.khe.kenthackenough.R;

/**
 *
 */
public class Event implements Comparable<Event>, Serializable{
    private static final String[] DAY_STRING_LOOK_UP = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private static final String[] MONTH_STRING_LOOK_UP = new String[]{"January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October", "November", "December"};

    private Calendar startCal = new GregorianCalendar();
    private Calendar endCal = new GregorianCalendar();
    private boolean notify = false;
    private String description;
    private String title;
    private String type;
    private String group;
    private String location;
    private Long[] id;

    //currently unused
    private Date notifyOn; // if null ignored
    private String notificationMessage;

    private int ScheduledNotificationID;

    private Color color;
    private int notificationId;
    private static int nextNotificationID = 0;
    private static final int WARNING = 10 * 60 * 1000; // amount of time in milliseconds to warn the user before an event

    public Event(Date start, Date end, String title, String type, String group, String description,
                 String location, Long[] id, boolean notify) {
        startCal.setTime(start);
        endCal.setTime(end);
        this.notify = notify;
        this.description = description;
        this.title = title;
        this.type = type;
        this.group = group;
        this.location = location;
        this.id = id;

        // schedule an event if we should notify
        if (notify) {
            Log.i("KHE2015", "scheduling notification");
            EventNotificationPoster.schedule(KHEApp.self, this, getStart().getTimeInMillis(), WARNING);
        }

    }

    public Event(Long[] id) {
        this.id = id;
    }

    public static Event getFromJSON(JSONObject json) throws JSONException {
        String title = json.getString("title");
        String description = json.optString("description");
        Date start = new DateTime(json.getString("start")).toDate();
        Date end = new DateTime(json.getString("end")).toDate();
        String type = json.optString("type");
        String location = json.optString("location");
        String group = json.optString("group");
        boolean notify = json.getBoolean("notify");

        String uuidString = json.getString("_id");
        Long[] id = new Long[2];
        id[0] = Long.decode('#' + uuidString.substring(0, 12));
        id[1] = Long.decode('#' + uuidString.substring(12));

        return new Event(start, end, title, type, group, description, location, id, notify);
    }

    public Calendar getStart() {
        return startCal;
    }

    public Calendar getEnd() {
        return endCal;
    }

    public String getFriendlyTimeRange() {
        return String.format("%tl:%tM %s - %tl:%tM %s",
                getStart(), getStart(), (getStart().get(Calendar.AM_PM) == 1)?"AM":"PM",
                getEnd(), getEnd(), (getEnd().get(Calendar.AM_PM) == 1)?"AM":"PM");
    }

    public String getDay() {
        return DAY_STRING_LOOK_UP[startCal.get(Calendar.DAY_OF_WEEK)-1];
    }
    public String getSimpleDate() {
        return MONTH_STRING_LOOK_UP[startCal.get(Calendar.MONTH)] +  " " + startCal.get(Calendar.DAY_OF_MONTH);
    }

    // adds a notification for the event
    public void notify(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.clover);
        builder.setContentTitle("Update from KHE");
        builder.setContentText(Html.fromHtml(title + " started"));
        builder.setAutoCancel(true);
        builder.setCategory("CATEGORY_MESSAGE");
        builder.setPriority(1);
        builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        builder.setWhen(this.startCal.getTimeInMillis());

        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("view", 2);



        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationId = nextNotificationID++;
        notificationManager.notify("events", notificationId, builder.build());
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getGroup() {
        return group;
    }

    public String getType() {
        return type;
    }

    public Drawable getIcon() {
        return null;
    }

    public String getLocation() {
        return location;
    }

    public Date getNotifyOn() {
        return notifyOn;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public int hashCode() {
        return id[1].intValue(); // todo better hash
    }


    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Event)) {
            return super.equals(other);
        }

        Event otherEvent = (Event) other;
        return this.id[0].equals(otherEvent.id[0]) && this.id[1].equals(otherEvent.id[1]);

    }

    @Override
    public int compareTo(Event another) {
        return this.startCal.compareTo(another.startCal);
    }

    public Long[] getID() {
        return id.clone();
    }
}
