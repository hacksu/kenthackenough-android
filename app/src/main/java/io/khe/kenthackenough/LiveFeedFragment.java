package io.khe.kenthackenough;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;


import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.github.rjeschke.txtmark.Processor;


/**
 * A simple {@link Fragment} subclass.
 */
public class LiveFeedFragment extends Fragment {
    private Timer mTimer = new Timer();

    public LiveFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_live_feed, container, false);
        ListView messages = (ListView) view.findViewById(R.id.messages);
        messages.setAdapter(new LiveFeedAdapter(getActivity(),Config.API_URL + "/messages"));
        return view;
    }

    @Override
    public void onStop () {
        super.onStop();
        if (MainActivity.queue != null) {
            MainActivity.queue.cancelAll("KHE");
        }
        mTimer.purge();
    }

    private class LiveFeedAdapter extends BaseAdapter implements ListAdapter {
        private List<Message> messages = new LinkedList<>();
        private Context context;

        private JsonObjectRequest getMessages;
        public LiveFeedAdapter(Context context, String url) {
            this.context = context;
            getMessages = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray JsonMessages = response.getJSONArray("messages");
                        for (int i = 0; i<JsonMessages.length(); ++i) {
                            JSONObject message = JsonMessages.getJSONObject(i);

                            String htmlMessage = Processor.process(message.getString("text"));
                            Message m = new Message(new DateTime(message.getString("created")).toDate(), htmlMessage);

                            // check if the message has already been added
                            if (!messages.contains(m)) {
                                messages.add(m); // todo add notifications
                            }
                        }
                        notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, null);

            getMessages.setTag("KHE"); // so we can track it and cancel it later



            mTimer.scheduleAtFixedRate(new TimerTask() {

                synchronized public void run() {
                    MainActivity.queue.add(getMessages);
                }

            }, 0, 30000);
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.live_feed_item,null);
            }

            Message m = messages.get(messages.size()-1-position); // reverse the direction
            TextView message = (TextView) view.findViewById(R.id.live_feed_message);
            TextView timeSince = (TextView) view.findViewById(R.id.live_feed_time);

            message.setText(Html.fromHtml(m.message));
            message.setMovementMethod(LinkMovementMethod.getInstance()); // this is needed to make links work

            timeSince.setText(m.timeSince());

            return view;
        }

        private class Message{
            public Date created;
            public String message;

            public Message(Date created, String message) {
                this.created = created;
                this.message = message;
            }

            public String timeSince() {
                Date now = new Date();
                long deltaMills = now.getTime() - created.getTime();
                long deltaSeconds = deltaMills/1000;
                long deltaMinutes = deltaSeconds/60;
                long deltaHours = deltaMinutes/60;
                long deltaDays = deltaHours/24;

                if (deltaDays > 0) {
                    return String.format("%d days ago", deltaDays);
                } else if (deltaHours != 0) {
                    return String.format("%d hours ago", deltaHours);
                } else if (deltaMinutes != 0) {
                    return String.format("%d minutes ago", deltaMinutes);
                } else if (deltaSeconds != 0) {
                    return String.format("%d seconds ago", deltaSeconds);
                } else {
                    return String.format("%d milliseconds ago", deltaMills);
                }
            }

            @Override
            public int hashCode() {
                return message.hashCode() ^ created.hashCode(); //not sure if this is best
            }

            @Override
            public boolean equals(Object other) {
                if (!(other instanceof Message)) {
                    return super.equals(other);
                }

                Message otherMessage = (Message) other;
                return this.message.equals(otherMessage.message) && this.created.equals(otherMessage.created);
            }
        }
    }


}
