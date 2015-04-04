package io.khe.kenthackenough;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.rjeschke.txtmark.Processor;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class to manager messages sent to participants and staff through the api.
 */
public class LiveFeedManager {
    private Request listMessages;
    public List<Message> messages = new LinkedList<>();
    private Timer timer = new Timer();
    private Set<NewMessagesListener> listeners = new HashSet<>();
    private int checkDelay;

    /**
     * Standard constructor for a LiveFeedManager (does not start it pulling)
     * @param url The url for the api including the protocol
     * @param checkDelay The time between requests to the server
     */
    public LiveFeedManager(String url, int checkDelay) {
        listMessages = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<Message> newMessages = new LinkedList<>();
                try {
                    JSONArray JsonMessages = response.getJSONArray("messages");
                    for (int i = JsonMessages.length()-1; i>=0; --i) {
                        JSONObject message = JsonMessages.getJSONObject(i);

                        String htmlMessage = Processor.process(message.getString("text"));
                        Message m = new Message(new DateTime(message.getString("created")).toDate(), htmlMessage);

                        // check if the message has already been added
                        if (!messages.contains(m)) {
                            messages.add(m);
                            newMessages.add(m);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Collections.sort(messages);
                Collections.sort(newMessages);

                for (NewMessagesListener listener : listeners) {
                    listener.newMessagesAdded(newMessages, messages);
                }
                System.out.println(messages.size());

            }
        }, null);
        this.checkDelay = checkDelay;
    }

    /**
     * Starts a repeated request to the server to fetch all messages
     */
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            synchronized public void run() {
                KHEApp.queue.add(listMessages);
            }

        }, 0, checkDelay);
    }

    /**
     * Stops the requests started by start()
     */
    public void halt() {
        timer.purge();
    }

    /**
     * Adds a listener
     *
     * @param listener The listener to be added
     */
    public void addListener(NewMessagesListener listener) {
        listeners.add(listener);
    }

    public interface NewMessagesListener {
        /**
         * Called when new messages are relieved from the server
         *
         * @param newMessages a list of new messages received ordered by time sent with the newest first
         * @param allMessages a list of all messages ordered by time sent with the newest first
         */
        void newMessagesAdded(List<Message> newMessages, List<Message> allMessages);
    }
}
