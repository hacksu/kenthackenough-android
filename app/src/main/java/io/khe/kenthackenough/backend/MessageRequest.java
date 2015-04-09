package io.khe.kenthackenough.backend;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.github.rjeschke.txtmark.Processor;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * A request to fetch a list of messages from a remote server
 */
public class MessageRequest extends Request<List<Message>> {

    Response.Listener<List<Message>> listener;

    public MessageRequest(String url, Response.ErrorListener errorListener,
                          Response.Listener<List<Message>> listener) {
        super(url, errorListener);
        this.listener = listener;
    }

    public MessageRequest(int method, String url, Response.ErrorListener errorListener,
                          Response.Listener<List<Message>> listener) {
        super(method, url, errorListener);
        this.listener = listener;
    }

    @Override
    protected Response<List<Message>> parseNetworkResponse(NetworkResponse netResponse) {
        List<Message> messages = new LinkedList<>();
        try {
            JSONObject response = new JSONObject(new String(netResponse.data,
                    HttpHeaderParser.parseCharset(netResponse.headers)));
            JSONArray JsonMessages = response.getJSONArray("messages");
            for (int i = JsonMessages.length()-1; i>=0; --i) {
                JSONObject message = JsonMessages.getJSONObject(i);
                messages.add(Message.getFromJSON(message));
            }
        } catch (JSONException e) {
            Log.e("KHE2015","failed to parse response from " + getUrl(), e);
            return Response.error(new VolleyError(netResponse));
        } catch (UnsupportedEncodingException e) {
            Log.e("KHE2015", "failed to parse response from " + getUrl(), e);
            return Response.error(new VolleyError(netResponse));
        }
        Collections.sort(messages);
        return Response.success(messages, HttpHeaderParser.parseCacheHeaders(netResponse));
    }

    @Override
    protected void deliverResponse(List<Message> response) {
        listener.onResponse(response);
    }
}
