package io.khe.kenthackenough.services;

import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

import io.khe.kenthackenough.Config;

/**
 * Created by Isaac on 9/24/2015.
 */
public class InstanceIdListener extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        GcmRegisterer.reRegister(this);
        Log.d(Config.DEBUG_TAG, "registered with server");
    }
}
