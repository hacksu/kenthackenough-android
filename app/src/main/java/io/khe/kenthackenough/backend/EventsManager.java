package io.khe.kenthackenough.backend;

import android.content.Context;
import android.os.Bundle;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.khe.kenthackenough.Config;
import io.khe.kenthackenough.GCM.GcmListener;
import io.khe.kenthackenough.GCM.GcmRegisterer;
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
    private String url;
    private Context applicationContext;

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

        this.url = url;

        uiThreadHandler = new Handler(context.getMainLooper());
        applicationContext = context;
    }

    /**
     * Starts a repeated request to the server to fetch all messages
     */
    public void start() {
        setUpGcm();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                update();

                // register with socket.io if GCM breaks for some reason
                if (!GcmRegisterer.working) {
                    if (socket == null || !socket.connected()) {
                        Log.w(Config.DEBUG_TAG, "GCM failed so falling back on socket.io");
                        setSocketIO();
                        socket.connect();
                    }
                } else {
                    if(socket != null && socket.connected()) {
                        Log.w(Config.DEBUG_TAG, "GCM came back so returning to it");
                        socket.disconnect();
                    }
                }
            }

        }, 0, checkDelay);
    }

    private void update() {
        KHEApp.queue.add(listEvents);
    }

    private void setUpGcm() {
        GcmListener.addListener(applicationContext, "events", "create", new GcmListener.GcmMessageListener() {
            @Override
            public void onReceive(Bundle message) {
                try {
                    Event newEvent = Event.getFromJSON(new JSONObject(message.getString("document")));
                    createEvent(newEvent);
                } catch (JSONException e) {
                    Log.e(Config.DEBUG_TAG, "failed to parse create message", e);
                }
            }
        });

        GcmListener.addListener(applicationContext, "events", "delete", new GcmListener.GcmMessageListener() {
            @Override
            public void onReceive(Bundle message) {
                try {
                    deleteEvent(new JSONObject(message.getString("document")).getString("_id"));
                } catch (JSONException e) {
                    Log.e(Config.DEBUG_TAG, "failed to parse delete message", e);
                }
            }
        });

        GcmListener.addListener(applicationContext, "events", "update", new GcmListener.GcmMessageListener() {
            @Override
            public void onReceive(Bundle message) {
                try {
                    Event updatedEvent = Event.getFromJSON(new JSONObject(message.getString("document")));
                    updateEvent(updatedEvent);
                } catch (JSONException e) {
                    Log.e(Config.DEBUG_TAG, "failed to parse update message", e);
                }
            }
        });
    }

    private void setSocketIO(){
        try {
            socket = IO.socket(url);

            socket.on("create", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject json = (JSONObject) args[0];
                    try {
                        final Event event = Event.getFromJSON(json);
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                createEvent(event);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(Config.DEBUG_TAG, "failed to parse create event", e);
                    }

                }
            });

            socket.on("delete", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject json = (JSONObject) args[0];
                    try {
                        final String uuidString = json.getString("_id");
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                deleteEvent(uuidString);
                            }
                        });

                    } catch (JSONException e) {
                        Log.e(Config.DEBUG_TAG, "failed to parse delete event", e);
                    }
                }
            });

            socket.on("update", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    Log.i(Config.DEBUG_TAG,"updating events");
                    try {
                        final Event updatedEvent = Event.getFromJSON((JSONObject) args[0]);
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateEvent(updatedEvent);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(Config.DEBUG_TAG, "failed to parse update event", e);
                    }
                }
            });
        } catch (URISyntaxException e) {
            Log.e(Config.DEBUG_TAG, "Failed to connect to " + url + " with socket.io");
        }
    }

    public Event getNextEvent() {
        Long now = System.currentTimeMillis();
        for(Event event: events) {
            if(event.getStart().getTime().getTime() > now) {
                return event;
            }
        }
        return null;
    }

    private void createEvent(Event newEvent) {
        events.add(0, newEvent);
        for (EventsUpdateListener listener : updateListeners) {
            listener.eventsFetched(events);
        }
    }

    private void deleteEvent(String uuidString) {

        events.remove(new Event(uuidString));
        for (EventsUpdateListener listener : updateListeners) {
            listener.eventsFetched(events);
        }
    }

    private void updateEvent(Event updatedEvent) {
        events.remove(updatedEvent);
        events.add(updatedEvent);
        Collections.sort(events);

        for (EventsUpdateListener listener : updateListeners) {
            listener.eventsFetched(events);
        }
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