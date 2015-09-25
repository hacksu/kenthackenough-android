/*
    A class to register the application with GCM so that it can receive messages.

   Based partially on an example here:
   https://github.com/googlesamples/google-services/blob/master/android/gcm/app/src/main/java/gcm/play/android/samples/com/gcmquickstart/RegistrationIntentService.java
 */


package io.khe.kenthackenough.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import io.khe.kenthackenough.Config;
import io.khe.kenthackenough.R;

public class GcmRegisterer extends IntentService {

    HttpClient httpClient = new DefaultHttpClient();


    public static void register(Context context) {
        Intent intent = new Intent(context, GcmRegisterer.class);
        context.startService(intent);
    }

    public static void reRegister(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean("Registered", false).apply();
        Intent intent = new Intent(context, GcmRegisterer.class);
        context.startService(intent);
    }

    public GcmRegisterer() {
        super("GcmRegisterer");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            // only register if we have not yet registered
            if(!sharedPreferences.getBoolean("Registered", false)) {
                deregisterWithBackend(token);
                registerWithBackend(token);
            }

            sharedPreferences.edit().putBoolean("Registered", true).apply();

        } catch (IOException e) {
            sharedPreferences.edit().putBoolean("Registered", false).apply();
            Log.e(Config.DEBUG_TAG, "failed to register", e);
        }
    }


    void deregisterWithBackend(String token) throws IOException {
        HttpDelete deristerRequest = new HttpDelete(Config.API_URL + "/devices/"+token);
        while(httpClient.execute(deristerRequest).getStatusLine().getStatusCode() == 200){
            Log.d(Config.DEBUG_TAG, "deregistered once");
        }
    }

    void registerWithBackend(String token) throws IOException {
        HttpPost registerRequest = new HttpPost(Config.API_URL + "/devices");

        registerRequest.setHeader("Content-type", "application/json");
        registerRequest.setEntity(new StringEntity("{\"id\": \"" + token + "\"}"));
        Log.d(Config.DEBUG_TAG, "response to registering is " +
                httpClient.execute(registerRequest).getStatusLine());
    }
}
