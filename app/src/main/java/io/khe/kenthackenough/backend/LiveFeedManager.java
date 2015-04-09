package io.khe.kenthackenough.backend;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.khe.kenthackenough.KHEApp;

/**
 * Class to manager messages sent to participants and staff through the api.
 */
public class LiveFeedManager {
    private Request listMessages;
    public List<Message> messages = new LinkedList<>();
    private Timer timer = new Timer();
    private Set<NewMessagesListener> listeners = new HashSet<>();
    private int checkDelay;

    private Socket socket;
    private Activity activity;


    /**
     * Standard constructor for a LiveFeedManager (does not start it pulling)
     * @param url The url for the api including the protocol
     * @param checkDelay The time between requests to the server
     */
    public LiveFeedManager(String url, int checkDelay, Activity activity) {
        listMessages = new MessageRequest(Request.Method.GET, url, null, new Response.Listener<List<Message>>() {
            @Override
            public void onResponse(List<Message> messagesFromServer) {

                long newestSaved = 0;
                if (messages.size() > 0) {
                    newestSaved = messages.get(0).getCreated().getTime();
                }
                List<Message> newMessages = new LinkedList<Message>();

                for (Message message: messagesFromServer) {
                    if (message.getCreated().getTime() > newestSaved) {
                        newMessages.add(message);
                    } else {
                        break; // because the list is sorted after the first new message there are no more
                    }

                }

                messages = messagesFromServer;


                for (NewMessagesListener listener : listeners) {
                    listener.newMessagesAdded(newMessages, messages);
                }
                System.out.println("found " + newMessages.size() + " new messages");

            }
        });
        this.checkDelay = checkDelay;
        this.activity = activity;

        try {
            socket = IO.socket(url);
        } catch (URISyntaxException e) {
            Log.e("KHE 2015", "API url " + url + " failed");
        }
    }

    /**
     * Starts a repeated request to the server to fetch all messages
     */
    public void start() {
        socket.on("create", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject json = (JSONObject) args[0];
                try {
                    final Message newMessage = Message.getFromJSON(json);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messages.add(newMessage);
                            List<Message> newMessages = new  ArrayList(1);
                            newMessages.add(newMessage);
                            for (NewMessagesListener listener : listeners) {
                                listener.newMessagesAdded(newMessages, messages);
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e("KHE2015", "failed to parse create for message", e);
                }

            }
        });
        socket.connect();

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
