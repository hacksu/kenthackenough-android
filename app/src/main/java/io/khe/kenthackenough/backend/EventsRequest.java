package io.khe.kenthackenough.backend;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

                String title = event.getString("title");
                String description = event.getString("description");
                Date start = new DateTime(event.getString("start")).toDate();
                Date end = new DateTime(event.getString("end")).toDate();
                String type = event.getString("type");
                String location = event.getString("location");
                String group = event.getString("group");
                Date notifyOn = null;
                if (event.getBoolean("notify")) {
                    notifyOn = (Date) start.clone();
                }
                Event e = new Event(start, end, title, type, group, description, location);
                events.add(e);

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
