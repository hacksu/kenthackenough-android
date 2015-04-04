package io.khe.kenthackenough;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

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
    }
}
