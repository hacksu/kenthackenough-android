package io.khe.kenthackenough;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.khe.kenthackenough.backend.Events.Event;

/**
 * EventNotificationPoster This is a small class designed to post events at a later date
 */
public class EventNotificationPoster  extends BroadcastReceiver {

    /**
     * schedule allows for easy scheduling of a future event's notification
     * @param event the event to schedule
     */
    public static PendingIntent schedule(Context context, Event event, long time, long warning) {

        if(time < System.currentTimeMillis()) {
            return null;
        }

        Intent intent = new Intent(context, EventNotificationPoster.class);
        intent.putExtra("event", event.getID());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, time - warning, pendingIntent);
        return pendingIntent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("KHE 2015", "triggering notification");

        String eventID = intent.getStringExtra("event");
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
            Log.w("KHE 2015", "event for id (" + eventID + ") not found by EventNotificationPoster");
            return;
        }

        event.notify(context);
    }
}
