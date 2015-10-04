package io.khe.kenthackenough;

import android.app.Application;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;

import io.khe.kenthackenough.backend.About.AboutManager;
import io.khe.kenthackenough.backend.Events.EventsManager;
import io.khe.kenthackenough.backend.Messages.LiveFeedManager;
import io.khe.kenthackenough.backend.Messages.Message;
import io.khe.kenthackenough.fragments.LiveFeedFragment;
import io.khe.kenthackenough.GCM.GcmRegisterer;

/**
 * Custom application to store data and service that must be shared between activities and persist
 * through the application's life cycle
 */
public class KHEApp extends Application {
    public LiveFeedManager liveFeedManager;
    public EventsManager eventsManager;
    public AboutManager aboutManager;

    public static RequestQueue queue;
    public static KHEApp self;
    public void onCreate() {
        super.onCreate();

        Log.e("KHE2015", "app started");

        // really bad idea probably, I'll do this better at some point
        self = this;

        queue = Volley.newRequestQueue(this);
        // start the LiveFeedManager
        liveFeedManager = new LiveFeedManager(Config.API_URL + "/messages", 120000, this);
        liveFeedManager.addNewMessagesListener(new LiveFeedManager.NewMessagesListener() {
            boolean first = true;

            @Override
            public void newMessagesAdded(List<Message> newMessages, List<Message> allMessages) {
                if (!LiveFeedFragment.mActive && !first) {
                    for (Message message : newMessages) {
                        message.notify(getApplicationContext());
                    }
                }
                first = false;
            }
        });

        liveFeedManager.addDeleteMessageListener(new LiveFeedManager.DeletedMessageListener() {
            @Override
            public void messageDeleted(Message deletedMessage, List<Message> allMessages) {
                if (deletedMessage != null) {
                    deletedMessage.closeNotification(getApplicationContext());
                }
            }
        });
        liveFeedManager.start();


        eventsManager = new EventsManager(Config.API_URL + "/events", 120000, this);
        eventsManager.start();

        aboutManager = new AboutManager(Config.API_URL + "/about", 120000, this);
        aboutManager.start();

        GcmRegisterer.register(this);
    }
}
