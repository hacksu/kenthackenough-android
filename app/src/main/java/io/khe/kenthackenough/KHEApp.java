package io.khe.kenthackenough;

import android.app.Application;
import android.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;

import io.khe.kenthackenough.backend.EventsManager;
import io.khe.kenthackenough.backend.LiveFeedManager;
import io.khe.kenthackenough.backend.Message;

/**
 * Custom application to store data and service that must be shared between activities and persist
 * through the application's life cycle
 */
public class KHEApp extends Application {
    public LiveFeedManager liveFeedManager;
    public EventsManager eventsManager;
    public Fragment[] views = new Fragment[3];

    public static RequestQueue queue;
    public void onCreate() {
        super.onCreate();
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
                deletedMessage.closeNotification(getApplicationContext());
            }
        });
        liveFeedManager.start();


        eventsManager = new EventsManager(Config.API_URL + "/events", 120000, this);
        eventsManager.start();

        views[0] = new DashboardFragment();
        views[1] = new LiveFeedFragment();
        views[2] = new EventsFragment();
    }
}
