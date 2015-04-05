package io.khe.kenthackenough.backend;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import java.util.Date;

/**
 *
 */
public class Event implements Comparable<Event>{
    private Date start;
    private Date finish;
    private String description;
    private String title;
    private String group;

    private Date notifyOn; // if null ignored
    private String notificationMessage;

    private Color color;

    public Event(Date start, Date end, String group, String title, String description) {
        this.start = start;
        this.finish = end;
        this.description = description;
        this.title = title;
        this.group = group;
    }

    public Event(Date start, Date end, String group, String title, String description, Date notifyOn,
                 String notificationMessage, Color color) {
        this.start = start;
        this.finish = end;
        this.description = description;
        this.title = title;
        this.group = group;

        this.notifyOn = notifyOn;
        this.notificationMessage = notificationMessage;
        this.color = color;
    }

    public Date getStart() {
        return start;
    }

    public Date getFinish() {
        return finish;
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
        return this.title.equals(otherEvent.title) && this.start.equals(((Event) other).start);

    }

    @Override
    public int compareTo(Event another) {
        return this.start.compareTo(another.start);
    }
}
