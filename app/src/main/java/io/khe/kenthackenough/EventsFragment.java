package io.khe.kenthackenough;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
        RecyclerView events = (RecyclerView) view.findViewById(R.id.events);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        events.setLayoutManager(llm);


        eventsManager = ((KHEApp) getActivity().getApplication()).eventsManager;
        events.setAdapter(new EventsAdapter(getActivity(), eventsManager));


        return view;
    }

    private class EventsAdapter extends RecyclerView.Adapter<EventViewHolder> {
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
        public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.event, viewGroup, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(EventViewHolder eventViewHolder, int i) {
            Event e = events.get(i);

            eventViewHolder.description.setText(e.getDescription());
            eventViewHolder.title.setText(e.getTitle());
            eventViewHolder.start.setText(e.getStart().toString());
            eventViewHolder.end.setText(e.getFinish().toString());

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
        protected TextView title;
        protected TextView start;
        protected TextView end;
        protected TextView description;

        public EventViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.event_title);
            start = (TextView) itemView.findViewById(R.id.event_start);
            end = (TextView) itemView.findViewById(R.id.event_end);
            description = (TextView) itemView.findViewById(R.id.event_description);
        }
    }


}
