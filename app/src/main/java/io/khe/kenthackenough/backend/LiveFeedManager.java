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
import java.util.ArrayList;
import java.util.Collections;
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
 * Class to manager messages sent to participants and staff through the api.
 */
public class LiveFeedManager {
    private Request listMessages;
    public volatile List<Message> messages = new LinkedList<>();
    private Timer timer = new Timer();

    private Set<NewMessagesListener> newMessagesListeners = new HashSet<>();
    private Set<UpdatedMessageListener> updatedMessageListeners = new HashSet<>();
    private Set<DeletedMessageListener> deletedMessageListeners = new HashSet<>();

    private int checkDelay;
    private Handler uiThreadHandler;
    private Context applicationContext;

    private Socket socket;
    private String url;


    /**
     * Standard constructor for a LiveFeedManager (does not start it pulling)
     * @param url The url for the API including the protocol
     * @param checkDelay The time between requests to the server
     */
    public LiveFeedManager(String url, int checkDelay, final Context context) {
        this.url = url;
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
                        break; // because the list is sorted, after the first old message there are no more
                    }
                }

                messages = messagesFromServer;


                for (NewMessagesListener listener : newMessagesListeners) {
                    listener.newMessagesAdded(newMessages, messages);
                }
                System.out.println("found " + newMessages.size() + " new messages");

            }
        });

        applicationContext = context;
        listMessages.setTag("listMessages");
        this.checkDelay = checkDelay;
        uiThreadHandler = new Handler(context.getMainLooper());
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
                    if (socket != null && socket.connected()) {
                        Log.w(Config.DEBUG_TAG, "GCM came back so returning to it");
                        socket.disconnect();
                    }
                }
            }

        }, 0, checkDelay);
    }

    private void setUpGcm() {
        GcmListener.addListener(applicationContext, "messages", "create", new GcmListener.GcmMessageListener() {
            @Override
            public void onReceive(Bundle message) {
                try {
                    Message newMessage = Message.getFromJSON(new JSONObject(message.getString("document")));
                    createMessage(newMessage);
                } catch (JSONException e) {
                    Log.e(Config.DEBUG_TAG, "failed to parse create message", e);
                }
            }
        });

        GcmListener.addListener(applicationContext, "messages", "delete", new GcmListener.GcmMessageListener() {
            @Override
            public void onReceive(Bundle message) {
                try {
                    deleteMessage(new JSONObject(message.getString("document")).getString("_id"));
                } catch (JSONException e) {
                    Log.e(Config.DEBUG_TAG, "failed to parse delete message", e);
                }
            }
        });

        GcmListener.addListener(applicationContext, "messages", "update", new GcmListener.GcmMessageListener() {
            @Override
            public void onReceive(Bundle message) {
                try {
                    Message updateMessage = Message.getFromJSON(new JSONObject(message.getString("document")));
                    updateMessage(updateMessage);
                } catch (JSONException e) {
                    Log.e(Config.DEBUG_TAG, "failed to parse update message", e);
                }
            }
        });
    }

    private void setSocketIO() {
        try {
            socket = IO.socket(url);

            socket.on("create", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    JSONObject json = (JSONObject) args[0];
                    try {
                        final Message newMessage = Message.getFromJSON(json);
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                createMessage(newMessage);
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
                    final JSONObject json = (JSONObject) args[0];
                    try {
                        final String uuidString = json.getString("_id");
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                deleteMessage(uuidString);
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
                        final Message newMessage = Message.getFromJSON((JSONObject) args[0]);
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                updateMessage(newMessage);
                            }
                        });
                    } catch (JSONException e) {
                        Log.e("KHE2015", "failed to parse update message", e);
                    }
                }
            });
        } catch (URISyntaxException e) {
            Log.e(Config.DEBUG_TAG, "Failed to connect to " + url + " with socket.io");
        }

    }

        private void update() {
        KHEApp.queue.add(listMessages);
    }

    private void createMessage(Message message) {
        messages.add(0, message);
        List<Message> newMessages = new  ArrayList<Message>(1);

        newMessages.add(message);
        for (NewMessagesListener listener : newMessagesListeners) {
            listener.newMessagesAdded(newMessages, messages);
        }
    }

    private void deleteMessage(String uuidString) {

        Message toDelete = Message.getByID(uuidString);
        if (toDelete != null) {
            toDelete.closeNotification(applicationContext);
        }

        messages.remove(toDelete);
        for (DeletedMessageListener listener : deletedMessageListeners) {
            listener.messageDeleted(Message.getByID(uuidString), messages);
        }
    }

    private void updateMessage(Message message) {
        messages.remove(message);
        messages.add(message);
        Collections.sort(messages);

        for (UpdatedMessageListener listener : updatedMessageListeners) {
            listener.messageUpdated(message, messages);
        }
    }

    /**
     * Adds a NewMessagesListener
     *
     * @param listener The listener to be added
     */
    public void addNewMessagesListener(NewMessagesListener listener) {
        newMessagesListeners.add(listener);
    }

    public boolean removeNewMessagesListener(NewMessagesListener listener) {
        return newMessagesListeners.remove(listener);
    }


    public void addDeleteMessageListener(DeletedMessageListener listener) {
        deletedMessageListeners.add(listener);
    }

    public boolean removeDeleteMessageListener(DeletedMessageListener listener) {
        return deletedMessageListeners.remove(listener);
    }


    public void addUpdateMessageListener(UpdatedMessageListener listener) {
        updatedMessageListeners.add(listener);
    }

    public boolean removeUpdatedMessageListener(UpdatedMessageListener listener) {
        return updatedMessageListeners.remove(listener);
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

    public interface DeletedMessageListener {
        /**
         * Called when messages are deleted via socket.io or GCM.
         *
         * @param deletedMessage the message that was deleted
         * @param allMessages a list of all messages ordered by time sent with the newest first
         */
        void messageDeleted(Message deletedMessage, List<Message> allMessages);
    }

    public interface UpdatedMessageListener {
        /**
         * Called when an message is edited via socket.io or GCM.
         *
         * @param updatedMessage the message that was edited
         * @param allMessages a list of all messages ordered by time sent with the newest first
         */
        void messageUpdated(Message updatedMessage, List<Message> allMessages);
    }
}
