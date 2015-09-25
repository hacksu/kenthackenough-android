package io.khe.kenthackenough.GCM;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import io.khe.kenthackenough.Config;

/**
 * GcmListener is a service which will receive messaging from the backend through GCM messaging
 */
public class GcmListener extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(Config.DEBUG_TAG, "got " + data);
    }

}
