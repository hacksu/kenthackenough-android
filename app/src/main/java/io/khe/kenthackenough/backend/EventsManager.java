package io.khe.kenthackenough.backend;

import com.android.volley.Request;
import com.android.volley.Response;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.khe.kenthackenough.KHEApp;

/**
 * Class to manager the list of events through the api.
 */
public class EventsManager {
    private Request listEvents;
    public List<Event> events = new LinkedList<>();
    private Timer timer = new Timer();
    private Set<EventsUpdateListener> updateListeners = new HashSet<>();
    private int checkDelay;

    /**
     * Standard constructor for a EventsManager (does not start it pulling)
     * @param url The url for the api including the protocol
     * @param checkDelay The time between requests to the server
     */
    public EventsManager(String url, int checkDelay) {
        listEvents = new EventsRequest(Request.Method.GET, url, null, new Response.Listener<List<Event>>() {
            @Override
            public void onResponse(List<Event> eventsFromServer) {
                events = eventsFromServer;

                for (EventsUpdateListener listener : updateListeners) {
                    listener.eventsFetched(events);
                }

            }
        });
        listEvents.setTag("listMessages");
        this.checkDelay = checkDelay;
    }

    /**
     * Starts a repeated request to the server to fetch all messages
     */
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            synchronized public void run() {
                KHEApp.queue.add(listEvents);
            }

        }, 0, checkDelay);
    }

    /**
     * Stops the requests started by start()
     */
    public void halt() {
        timer.purge();
        KHEApp.queue.cancelAll("listMessages");
    }

    /**
     * Adds a listener
     *
     * @param listener The listener to be added
     */
    public void addListener(EventsUpdateListener listener) {
        updateListeners.add(listener);
    }

    public interface EventsUpdateListener {
        /**
         * Called when a list of Events is fetched from the server
         *
         * @param events a list of new messages received ordered by time sent with the newest first
         */
        void eventsFetched(List<Event> events);
    }
}