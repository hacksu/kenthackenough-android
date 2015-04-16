package io.khe.kenthackenough.backend;

import android.graphics.Color;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 */
public class Event implements Comparable<Event>{
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
