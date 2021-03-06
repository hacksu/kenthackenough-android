package io.khe.kenthackenough.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.khe.kenthackenough.KHEApp;
import io.khe.kenthackenough.R;
import io.khe.kenthackenough.backend.Events.Event;
import io.khe.kenthackenough.backend.Events.EventsManager;


/**
 * Should eventually show a list of events for the hackathon
 */
public class EventsFragment extends Fragment {


    private EventsManager eventsManager;
    private SwipeRefreshLayout refresh;
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
        refresh = ((SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh));

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                eventsManager.halt();
                eventsManager.start();
            }
        });
        eventsManager.addListener(new EventsManager.EventsUpdateListener() {
            @Override
            public void eventsFetched(List<Event> events) {
                refresh.setRefreshing(false);
            }
        });
        eventsManager.addErrorListener(new EventsManager.EventErrorListener() {
            @Override
            public void error(Object error) {
                refresh.setRefreshing(false);
                Toast.makeText(getActivity(), "Error Fetching Updates", Toast.LENGTH_SHORT).show();
            }
        });


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
                    inflate(R.layout.events_item, viewGroup, false);
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
        private TextView location;
        private ImageView icon;

        private TextView week_day;
        private TextView date;

        private RelativeLayout header;
        private LinearLayout mainView;

        public EventViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.event_title);
            times = (TextView) itemView.findViewById(R.id.event_time_range);
            description = (TextView) itemView.findViewById(R.id.event_description);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            location = (TextView) itemView.findViewById(R.id.event_location);
            this.mainView = (LinearLayout) itemView;
        }

        /**
         * Sets the view to match the information in event
         * @param event the event to match
         */
        public void setFromEvent(Event event) {
            title.setText(event.getTitle());
            times.setText(event.getFriendlyTimeRange());
            location.setText(event.getLocation());
            description.setText(event.getDescription());
        }

        public void createDayHeader(Event e) {
            this.header = (RelativeLayout) LayoutInflater.from(mainView.getContext()).
                    inflate(R.layout.day_header, mainView, false);

            week_day = (TextView) header.findViewById(R.id.day_of_week);
            this.date = (TextView) header.findViewById(R.id.date);

            mainView.addView(header, 0);

            Spannable weekDayUnderlined = new SpannableString(e.getDay());
            weekDayUnderlined.setSpan(new UnderlineSpan(), 0, weekDayUnderlined.length(), 0);

            this.week_day.setText(weekDayUnderlined);
            this.date.setText(e.getSimpleDate());
        }
        public void removeDayHeader() {
            mainView.removeView(header);
        }
    }


}
