package io.khe.kenthackenough.backend.Events;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import io.khe.kenthackenough.backend.Events.Event;

/**
 * Request to return all the events on the server
 */
public class EventsRequest extends Request<List<Event>> {

    Response.Listener<List<Event>> listener;

    public EventsRequest(int method, String url, Response.ErrorListener errorListener,
                          Response.Listener<List<Event>> listener) {
        super(method, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response<List<Event>> parseNetworkResponse(NetworkResponse netResponse) {
        List<Event> events = new LinkedList<>();
        try {
            JSONObject response = new JSONObject(new String(netResponse.data,
                    HttpHeaderParser.parseCharset(netResponse.headers)));
            JSONArray JsonEvents = response.getJSONArray("events");

            for (int i = 0; i<JsonEvents.length(); ++i) {
                JSONObject event = JsonEvents.getJSONObject(i);
                events.add(Event.getFromJSON(event));
            }
        } catch (JSONException e) {
            Log.e("KHE2015", "failed to parse response from " + getUrl(), e);
            return Response.error(new VolleyError(netResponse));
        } catch (UnsupportedEncodingException e) {
            Log.e("KHE2015","failed to parse response from " + getUrl(), e);
            return Response.error(new VolleyError(netResponse));
        }
        Collections.sort(events);
        return Response.success(events, HttpHeaderParser.parseCacheHeaders(netResponse));
    }

    @Override
    protected void deliverResponse(List<Event> response) {
        listener.onResponse(response);
    }
}
