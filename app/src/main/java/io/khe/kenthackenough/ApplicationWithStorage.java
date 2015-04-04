package io.khe.kenthackenough;

import android.app.ActivityManager;
import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;

/**
 * Created by isaac on 4/3/15.
 */
public class ApplicationWithStorage extends Application {
    public LiveFeedManager liveFeedManager;
    public static RequestQueue queue;
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(this);
        // start the LiveFeedManager
        liveFeedManager = new LiveFeedManager(Config.API_URL + "/messages",10000);
        liveFeedManager.start();

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
