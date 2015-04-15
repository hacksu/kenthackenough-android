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
        double deltaMills = System.currentTimeMillis() - time;
        double deltaSeconds = deltaMills / 1000;
        double deltaMinutes = deltaSeconds / 60;
        double deltaHours = deltaMinutes / 60;
        double deltaDays = deltaHours / 24;

        String noun;
        long value;

        if (deltaDays > 0.5) {
            noun = "a day";
            value = (long)(deltaDays + 0.5);
        } else if (deltaHours > 1) {
            noun = "an hour";
            value = (long)(deltaHours + 0.5);
        } else if (deltaMinutes > 0.5) {
            noun = "a minute";
            value = (long)(deltaMinutes + 0.5);
        } else if (deltaSeconds > 0.5) {
            noun = "a second";
            value = (long)(deltaSeconds+ 0.5);
        } else {
            noun = "a second";
            value = 1;
        }

        if (value == 1) {
            return "About " + noun + " ago";
        } else {
            return Long.toString(value) + " " + noun + "s ago";
        }
    }

}
