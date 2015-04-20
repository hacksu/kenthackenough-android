package io.khe.kenthackenough.backend;

import android.graphics.Color;

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
    private String group;

    private Date notifyOn; // if null ignored
    private String notificationMessage;

    private Color color;

    public Event(Date start, Date end, String group, String title, String description) {
        startCal.setTime(start);
        endCal.setTime(end);
        this.description = description;
        this.title = title;
        this.group = group;
    }

    public Event(Date start, Date end, String group, String title, String description, Date notifyOn,
                 String notificationMessage, Color color) {
        startCal.setTime(start);
        endCal.setTime(end);
        this.description = description;
        this.title = title;
        this.group = group;

        this.notifyOn = notifyOn;
        this.notificationMessage = notificationMessage;
        this.color = color;
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
