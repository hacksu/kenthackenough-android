package io.khe.kenthackenough;


import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.khe.kenthackenough.backend.Event;
import io.khe.kenthackenough.backend.EventsManager;


/**
 * Should eventually show a list of events for the hackathon
 */
public class EventsFragment extends Fragment {


    private EventsManager eventsManager;
    private boolean first = true;

    public EventsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events, container, false);
        RecyclerView events = (RecyclerView) view.findViewById(R.id.events);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        events.setLayoutManager(llm);


        eventsManager = ((KHEApp) getActivity().getApplication()).eventsManager;
        EventsAdapter adapter = new EventsAdapter(eventsManager);
        events.setAdapter(adapter);
        if((savedInstanceState == null || savedInstanceState.getBoolean("new", first)) && first) {
            Date now = new Date();
            int i = 0;
            first = false;
            for (Event event : adapter.events) {
                if (event.getStart().getTime().compareTo(now) > 0) {
                    events.scrollToPosition(i);
                    break;
                }
                ++i;
            }
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("new", false);
    }

    private class EventsAdapter extends RecyclerView.Adapter<EventViewHolder> {
        public List<Event> events;
        private EventsAdapter self = this;


        public EventsAdapter(EventsManager manager) {
            manager.addListener(new EventsManager.EventsUpdateListener() {
                @Override
                public void eventsFetched(List<Event> events) {
                    self.events = events;
                    notifyDataSetChanged();
                }
            });
            this.events = manager.events;
            notifyDataSetChanged();
        }


        @Override
        public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.event, viewGroup, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(EventViewHolder eventViewHolder, int i) {
            Event e = events.get(i);

            if (i == 0 || events.get(i-1).getStart().get(Calendar.DAY_OF_YEAR) != e.getStart().get(Calendar.DAY_OF_YEAR)) {
                eventViewHolder.createDayHeader(e);
            }

            eventViewHolder.setFromEvent(e);
        }
        @Override
        public void onViewRecycled(EventViewHolder viewHolder) {
            viewHolder.removeDayHeader();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return events.size();
        }
    }

    private class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView times;
        private TextView description;

        private TextView week_day;
        private TextView date;

        private RelativeLayout header;
        private LinearLayout mainView;

        public EventViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.event_title);
            times = (TextView) itemView.findViewById(R.id.event_times);
            description = (TextView) itemView.findViewById(R.id.event_description);
            this.mainView = (LinearLayout) itemView;
        }

        /**
         * Sets the view to match the information in event
         * @param event the event to match
         */
        public void setFromEvent(Event event) {
            title.setText(event.getTitle());
            times.setText(event.getFriendlyTimeRange());
            description.setText(event.getDescription());
        }

        public void createDayHeader(Event e) {
            this.header = (RelativeLayout) LayoutInflater.from(mainView.getContext()).
                    inflate(R.layout.day_header, mainView, false);

            week_day = (TextView) header.findViewById(R.id.day_of_week);
            this.date = (TextView) header.findViewById(R.id.date);

            mainView.addView(header, 0);

            this.week_day.setText(e.getDay());
            this.date.setText(e.getSimpleDate());
        }
        public void removeDayHeader() {
            mainView.removeView(header);
        }
    }


}
