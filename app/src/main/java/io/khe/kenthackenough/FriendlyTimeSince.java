package io.khe.kenthackenough;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * A view to render a string such as "A minute ago"
 * dynamically based on a supplied date from setTime(long time)
 */
public class FriendlyTimeSince extends TextView {

    private long time = System.currentTimeMillis();
    private Handler timer = new Handler();
    private final FriendlyTimeSince self = this;
    private Runnable updateMessage = new Runnable() {
        @Override
        public void run() {
            self.setText(self.getFriendlyTimeSince());
            timer.postDelayed(this, nextChange());
        }
    };

    public FriendlyTimeSince(Context context) {
        super(context);
        timer.postDelayed(updateMessage, 0);
    }

    public FriendlyTimeSince(Context context, AttributeSet attrs) {
        super(context, attrs);
        String timeString = attrs.getAttributeValue("app","time");
        if (timeString != null) {
            time = Long.parseLong(timeString);
        }
        timer.postDelayed(updateMessage, 0);
    }

    public FriendlyTimeSince(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        String timeString = attrs.getAttributeValue("app","time");
        if (timeString != null) {
            time = Long.parseLong(timeString);
        }
        timer.postDelayed(updateMessage, 0);
    }

    /**
     * Use to set the time to calculate the message based on
     * @param time the time in milliseconds
     */
    public void setTime(long time) {
        this.time = time;
        timer.removeCallbacks(updateMessage);
        timer.postDelayed(updateMessage, 0);
    }

    public long getTime() {
        return time;
    }

    private int nextChange() {
        long deltaMills = System.currentTimeMillis() - time;
        long deltaSeconds = deltaMills / 1000;
        long deltaMinutes = deltaSeconds / 60;
        long deltaHours = deltaMinutes / 60;
        long deltaDays = deltaHours / 24;

        String noun;
        long value;

        if (deltaDays > 0) {
            return 86400000; // one day worth of milliseconds
        } else if (deltaHours != 0) {
            return 3600000; // one hour worth of milliseconds
        } else if (deltaMinutes != 0) {
            return 60000; // one minute worth of milliseconds
        } else {
            return 1000; //never update faster than one a second
        }
    }

    private String getFriendlyTimeSince() {
        long deltaMills = System.currentTimeMillis() - time;
        long deltaSeconds = deltaMills / 1000;
        long deltaMinutes = deltaSeconds / 60;
        long deltaHours = deltaMinutes / 60;
        long deltaDays = deltaHours / 24;

        String noun;
        long value;

        if (deltaDays > 0) {
            noun = "day";
            value = deltaDays;
        } else if (deltaHours > 0) {
            noun = "hour";
            value = deltaHours;
        } else if (deltaMinutes > 0) {
            noun = "minute";
            value = deltaMinutes;
        } else if (deltaSeconds > 0) {
            noun = "second";
            value = deltaSeconds;
        } else {
            noun = "second";
            value = 1;
        }

        if (value == 1) {
            return "About a" + (noun.charAt(0) == 'h' ? "n " : " ") + noun + " ago";
        } else {
            return Long.toString(value) + " " + noun + "s ago";
        }
    }

}
