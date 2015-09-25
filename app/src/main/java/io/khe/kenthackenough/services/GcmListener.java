package io.khe.kenthackenough.services;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import io.khe.kenthackenough.Config;

/**
 * Created by Isaac on 9/24/2015.
 */
public class GcmListener extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(Config.DEBUG_TAG, "got " + data);
    }

}
