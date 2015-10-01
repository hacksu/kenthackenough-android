package io.khe.kenthackenough.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import io.khe.kenthackenough.FriendlyTimeSince;
import io.khe.kenthackenough.KHEApp;
import io.khe.kenthackenough.R;
import io.khe.kenthackenough.backend.Event;
import io.khe.kenthackenough.backend.EventsManager;
import io.khe.kenthackenough.backend.LiveFeedManager;
import io.khe.kenthackenough.backend.Message;


public class DashboardFragment extends Fragment {
    LiveFeedManager messagesManager;
    EventsManager eventsManager;

    View message;
    View event;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        KHEApp app = (KHEApp) getActivity().getApplication();

        messagesManager = app.liveFeedManager;
        eventsManager = app.eventsManager;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        message = view.findViewById(R.id.newest_message);
        event = view.findViewById(R.id.next_event);

        message.setVisibility(View.INVISIBLE);
        event.setVisibility(View.INVISIBLE);

        if (messagesManager.messages.size() > 0) {
            getViewForMessage(messagesManager.messages.get(0), message);
            message.setVisibility(View.VISIBLE);

        }


        Event nextEvent = eventsManager.getNextEvent();
        if (nextEvent != null) {
            getViewForEvent(nextEvent, event);
            event.setVisibility(View.VISIBLE);
        }
        registerMessagesManagerListeners();
        registerEventsManagerListeners();


        return view;
    }

    void registerMessagesManagerListeners() {
        messagesManager.addNewMessagesListener(new LiveFeedManager.NewMessagesListener() {
            @Override
            public void newMessagesAdded(List<Message> newMessages, List<Message> allMessages) {
                message.setVisibility(View.VISIBLE);
                getViewForMessage(messagesManager.messages.get(0), message);
            }
        });
        messagesManager.addDeleteMessageListener(new LiveFeedManager.DeletedMessageListener() {
            @Override
            public void messageDeleted(Message deletedMessage, List<Message> allMessages) {
                if (messagesManager.messages.size() > 0) {
                    getViewForMessage(messagesManager.messages.get(0), message);
                    message.setVisibility(View.VISIBLE);
                } else {
                    message.setVisibility(View.INVISIBLE);
                }
            }
        });
        messagesManager.addUpdateMessageListener(new LiveFeedManager.UpdatedMessageListener() {
            @Override
            public void messageUpdated(Message updatedMessage, List<Message> allMessages) {
                getViewForMessage(messagesManager.messages.get(0), message);
            }
        });
    }

    void registerEventsManagerListeners() {
        eventsManager.addListener(new EventsManager.EventsUpdateListener() {
            @Override
            public void eventsFetched(List<Event> events) {
                Event nextEvent = eventsManager.getNextEvent();
                if (nextEvent != null) {
                    getViewForEvent(nextEvent, event);
                    event.setVisibility(View.VISIBLE);
                } else {
                    event.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private View getViewForMessage(Message m, View from) {
        if (from == null) {
           from = getActivity().getLayoutInflater().inflate(R.layout.live_feed_item, null);
        }

        TextView body = (TextView) from.findViewById(R.id.live_feed_message);
        FriendlyTimeSince time = (FriendlyTimeSince) from.findViewById(R.id.live_feed_time);

        body.setText(m.getFormatted());
        time.setTime(m.getCreated().getTime());

        return from;
    }

    private View getViewForEvent(Event e, View from) {
        if (from == null) {
            from = getActivity().getLayoutInflater().inflate(R.layout.event, null);
        }

        TextView title = (TextView) from.findViewById(R.id.event_title);
        TextView times = (TextView) from.findViewById(R.id.event_time_range);
        TextView description = (TextView) from.findViewById(R.id.event_description);
        TextView type = (TextView) from.findViewById(R.id.event_type);
        ImageView icon = (ImageView) from.findViewById(R.id.icon);


        title.setText(e.getTitle());
        times.setText(e.getFriendlyTimeRange());
        type.setText(e.getType());
        description.setText(e.getDescription());

        return from;
    }
}
