package io.khe.kenthackenough;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import io.khe.kenthackenough.backend.Event;
import io.khe.kenthackenough.backend.EventsManager;


/**
 * Should eventually show a list of events for the hackathon
 */
public class EventsFragment extends Fragment {


    private EventsManager eventsManager;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);
        ListView events = (ListView) view.findViewById(R.id.events);


        eventsManager = ((KHEApp) getActivity().getApplication()).eventsManager;
        events.setAdapter(new EventsAdapter(getActivity(), eventsManager));


        return view;
    }

    private class EventsAdapter extends BaseAdapter implements ListAdapter {
        private List<Event> events;
        private Context context;
        private EventsAdapter self = this;


        public EventsAdapter(Context context, EventsManager manager) {
            manager.addListener(new EventsManager.EventsUpdateListener() {
                @Override
                public void eventsFetched(List<Event> events) {
                    self.events = events;
                    notifyDataSetChanged();
                }
            });
            this.events = manager.events;
            this.context = context;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (events == null) return 0;
            else return events.size();
        }

        @Override
        public Object getItem(int position) {
            return events.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.event,null);
            }

            Event e = events.get(position);
            TextView title = (TextView) view.findViewById(R.id.event_title);
            TextView start = (TextView) view.findViewById(R.id.event_start);
            TextView end = (TextView) view.findViewById(R.id.event_end);
            TextView description = (TextView) view.findViewById(R.id.event_description);

            title.setText(e.getTitle());
            start.setText(e.getStart().toString());
            end.setText(e.getFinish().toString());
            description.setText(e.getDescription());

            return view;
        }
    }


}
