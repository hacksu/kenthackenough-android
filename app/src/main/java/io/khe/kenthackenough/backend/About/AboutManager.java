package io.khe.kenthackenough.backend.About;

import android.content.Context;
import android.text.SpannableString;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.github.rjeschke.txtmark.Processor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import io.khe.kenthackenough.Config;
import io.khe.kenthackenough.KHEApp;
import io.khe.kenthackenough.backend.Utilities;

/**
 * A class to manage the about page as served by the KHE page
 */
public class AboutManager {
    private Request getAbout;
    public About about;
    private Timer timer = new Timer();
    private Set<AboutUpdateListener> updateListeners = new HashSet<>();
    private int checkDelay;

    /**
     * Standard constructor for a AboutManager (does not start it pulling)
     * @param url The url for the api including the protocol
     * @param checkDelay The time between requests to the server
     */
    public AboutManager(String url, int checkDelay, final Context context) {
        getAbout = new AboutRequest(Request.Method.GET, url, null, new Response.Listener<About>() {
            @Override
            public void onResponse(About response) {
                about = response;
                for(AboutUpdateListener updateListener: updateListeners) {
                    updateListener.aboutUpdated(response);
                }
            }
        });

        // we aren't currently using this but it will allow us to remove these requests later if need
        this.checkDelay = checkDelay;
    }

    /**
     * Starts a repeated request to the server to fetch all messages
     */
    public void start() {
        timer.scheduleAtFixedRate(new TimerTask() {
            synchronized public void run() {
                KHEApp.queue.add(getAbout);
            }
        }, 0, checkDelay);
    }

    /**
     * Stops the requests started by start()
     */
    public void halt() {
        timer.purge();
        KHEApp.queue.cancelAll("getAbout");
    }

    /**
     * Adds a listener
     *
     * @param listener The listener to be added
     */
    public void addListener(AboutUpdateListener listener) {
        Log.i(Config.DEBUG_TAG, "added listener");
        updateListeners.add(listener);
    }

    public interface AboutUpdateListener {
        /**
         * Called when a new version of the about page is fetched from the server.
         *
         * @param about a list of new messages received ordered by time sent with the newest first
         */
        void aboutUpdated(About about);
    }

    public class AboutRequest extends Request<About> {

        Response.Listener<About> listener;


        public AboutRequest(int method, String url, Response.ErrorListener errorListener,
                            Response.Listener<About> listener) {
            super(method, url, errorListener);
            this.listener = listener;
        }

        @Override
        protected Response<About> parseNetworkResponse(NetworkResponse netResponse) {
            try {
                JSONObject json = new JSONObject(new String(netResponse.data,
                        HttpHeaderParser.parseCharset(netResponse.headers)));
                return Response.success(new About(json.getString("text")), HttpHeaderParser.parseCacheHeaders(netResponse));
            } catch (JSONException e) {
                Log.e(Config.DEBUG_TAG, "failed to parse response from " + getUrl(), e);
                return Response.error(new VolleyError(netResponse));
            } catch (UnsupportedEncodingException e) {
                Log.e(Config.DEBUG_TAG, "failed to parse response from " + getUrl(), e);
                return Response.error(new VolleyError(netResponse));
            }
        }

        @Override
        protected void deliverResponse(About response) {
            listener.onResponse(response);
        }
    }

    public class About {
        public String markdown;
        public String html;
        public SpannableString formatted;

        About(String markdown) {
            this.markdown = markdown;
            this.html = Processor.process(markdown);
            this.formatted = Utilities.getSpannableFromHTML(html);
        }
    }
}