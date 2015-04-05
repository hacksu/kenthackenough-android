package io.khe.kenthackenough;

import android.app.ActivityManager;
import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;

import io.khe.kenthackenough.backend.EventsManager;
import io.khe.kenthackenough.backend.LiveFeedManager;
import io.khe.kenthackenough.backend.Message;

/**
 * Created by isaac on 4/3/15.
 */
public class KHEApp extends Application {
    public LiveFeedManager liveFeedManager;
    public EventsManager eventsManager;

    public static RequestQueue queue;
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(this);
        // start the LiveFeedManager
        liveFeedManager = new LiveFeedManager(Config.API_URL + "/messages", 10000);
        eventsManager = new EventsManager(Config.API_URL + "/events", 120000);
        liveFeedManager.start();
        eventsManager.start();

        final Application self = this;
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        liveFeedManager.addListener(new LiveFeedManager.NewMessagesListener() {
            boolean first = true;
            @Override
            public void newMessagesAdded(List<Message> newMessages, List<Message> allMessages) {
                if (!LiveFeedFragment.mActive && !first){
                    for (Message message : newMessages) {
                        message.notify(self);
                    }
                }
                first = false;
            }
        });
    }
}
