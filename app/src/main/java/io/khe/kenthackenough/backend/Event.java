package io.khe.kenthackenough.backend;

import android.graphics.Color;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 */
public class Event implements Comparable<Event>{
    private static final String[] DAY_STRING_LOOK_UP = new String[]{"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private static final String[] MONTH_STRING_LOOK_UP = new String[]{"January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October", "November", "December"};

    private Calendar startCal = new GregorianCalendar();
    private Calendar endCal = new GregorianCalendar();
    private String description;
    private String title;
    private String type;
    private String group;
    private String location;

    //currently unused
    private Date notifyOn; // if null ignored
    private String notificationMessage;

    private Color color;

    public Event(Date start, Date end, String title, String type, String group, String description, String location) {
        startCal.setTime(start);
        endCal.setTime(end);
        this.description = description;
        this.title = title;
        this.type = type;
        this.group = group;
        this.location = location;
    }

    public static Event getFromJSON(JSONObject json) throws JSONException {
        String title = json.getString("title");
        String description = json.getString("description");
        Date start = new DateTime(json.getString("start")).toDate();
        Date end = new DateTime(json.getString("end")).toDate();
        String type = json.getString("type");
        String location = json.getString("location");
        String group = json.getString("group");

        return new Event(start, end, title, type, group, description, location);
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
    public boolean equals(Object other) {
        if (!(other instanceof Event)) {
            return super.equals(other);
        }

        Event otherEvent = (Event) other;
        return this.title.equals(otherEvent.title) && this.startCal.equals(((Event) other).startCal);

    }

    @Override
    public int compareTo(Event another) {
        return this.startCal.compareTo(another.startCal);
    }
}
