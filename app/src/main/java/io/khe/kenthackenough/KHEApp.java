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
        eventsManager = new EventsManager(Config.API_URL + "/events", 120000);
        eventsManager.start();

        final Application self = this;
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    }
}
