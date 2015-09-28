package io.khe.kenthackenough.GCM;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.khe.kenthackenough.Config;

/**
 * GcmListener is a service which will receive messaging from the backend through GCM messaging
 */
public class GcmListener extends GcmListenerService {
    Handler mainUIHandler = new Handler(Looper.getMainLooper());

    private static Map<String, List<GcmMessageListener>> listenersForTopic = new HashMap<>();

    private static volatile int notificationId = 0;
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(Config.DEBUG_TAG, "got " + data);
        //displayNotification(data, this);
        notifyMessage(data);
    }

    public static void addListener(Context context, String topic, GcmMessageListener listener) {
        List<GcmMessageListener> listeners = listenersForTopic.get(topic);
        if (listeners == null) {
            listeners = new LinkedList<>();
        }

        listeners.add(listener);
        listenersForTopic.put(topic, listeners);

        GcmRegisterer.subscribe(context, topic);
    }

    private void notifyMessage(final Bundle message) {

        // hacky but we're doing all the inter service communication on the main thread.
        mainUIHandler.post(new Runnable() {
            @Override
            public void run() {
                for(List<GcmMessageListener> listeners: listenersForTopic.values()) {
                    for (GcmMessageListener listener : listeners) {
                        listener.onReceive(message);
                    }
                }
            }
        });
    }

    public interface GcmMessageListener {
        void onReceive(Bundle message);
    }
}
