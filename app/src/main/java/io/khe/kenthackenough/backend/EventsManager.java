package io.khe.kenthackenough.backend;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collections;
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

    private Handler uiThreadHandler;
    private Socket socket;

    /**
     * Standard constructor for a EventsManager (does not start it pulling)
     * @param url The url for the api including the protocol
     * @param checkDelay The time between requests to the server
     */
    public EventsManager(String url, int checkDelay, final Context context) {
        listEvents = new EventsRequest(Request.Method.GET, url, null, new Response.Listener<List<Event>>() {
            @Override
            public void onResponse(List<Event> eventsFromServer) {
                events = eventsFromServer;

                for (EventsUpdateListener listener : updateListeners) {
                    listener.eventsFetched(events);
                }

            }
        });
        // we aren't currently using this but it will allow us to remove these requests later if need
        listEvents.setTag("listMessages");
        this.checkDelay = checkDelay;

        uiThreadHandler = new Handler(context.getMainLooper());

        try {
            socket = IO.socket(url);
        } catch (URISyntaxException e) {
            Log.e("KHE 2015", "API url " + url + " failed");
        }

        socket.on("create", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject json = (JSONObject) args[0];
                try {
                    final Event event = Event.getFromJSON(json);
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            events.add(0, event);
                            for (EventsUpdateListener listener : updateListeners) {
                                listener.eventsFetched(events);
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e("KHE2015", "failed to parse create message", e);
                }

            }
        });

        socket.on("delete", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject json = (JSONObject) args[0];
                try {
                    String uuidString = json.getString("_id");
                    final Long[] id = new Long[2];
                    id[0] = Long.decode('#' + uuidString.substring(0, 12));
                    id[1] = Long.decode('#' + uuidString.substring(12));

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            events.remove(new Event(id));
                            for (EventsUpdateListener listener : updateListeners) {
                                listener.eventsFetched(events);
                            }
                        }
                    });

                } catch (JSONException e) {
                    Log.e("KHE2015", "failed to parse delete message", e);
                }
            }
        });

        socket.on("update", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                try {
                    final Event updatedEvent = Event.getFromJSON((JSONObject) args[0]);
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            events.remove(updatedEvent);
                            events.add(updatedEvent);
                            Collections.sort(events);

                            for (EventsUpdateListener listener : updateListeners) {
                                listener.eventsFetched(events);
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e("KHE2015", "failed to parse update message", e);
                }
            }
        });
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