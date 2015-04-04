package io.khe.kenthackenough;

import java.util.Date;

class Message implements Comparable<Message> {
    public Date created;
    public String message;

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
}