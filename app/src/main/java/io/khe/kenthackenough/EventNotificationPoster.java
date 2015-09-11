package io.khe.kenthackenough;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.khe.kenthackenough.backend.Event;

/**
 * EventNotificationPoster This is a small class designed to post events at a later date
 */
public class EventNotificationPoster  extends BroadcastReceiver {

    private static int max_id = 0;
    /**
     * schedule allows for easy scheduling of a future event's notification
     * @param event the event to schedule
     */
    public static int schedule(Context context, Event event, long time, long warning) {

        if(time < System.currentTimeMillis()) {
            return -1;
        }

        Intent intent = new Intent(context, EventNotificationPoster.class);
        intent.putExtra("event", event.getID());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, max_id, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, time - warning, pendingIntent);
        return max_id++;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("KHE 2015", "triggering notification");

        Long[] eventID = (Long[]) intent.getSerializableExtra("event");
        Event idOnly = new Event(eventID);
        KHEApp app = KHEApp.self;
        Event event = null;
        for (Event e: app.eventsManager.events){
            if (e.equals(idOnly)) {
                event = e;
                break;
            }
        }

        // make sure we found the event possible reasons we wouldn't include it no longer existing
        if (event == null) {
            Log.w("KHE 2015", "event for id (" + eventID.toString() + ") not found by EventNotificationPoster");
            return;
        }

        event.notify(context);
    }
}
