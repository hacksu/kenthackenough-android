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

import java.util.List;
import java.util.Timer;


/**
 * A simple {@link Fragment} subclass.
 */
public class LiveFeedFragment extends Fragment {
    private Timer mTimer = new Timer();
    private LiveFeedManager liveFeedManager;

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

        liveFeedManager = ((ApplicationWithStorage) getActivity().getApplication()).liveFeedManager;
        messages.setAdapter(new LiveFeedAdapter(getActivity(), liveFeedManager));
        return view;
    }

    private class LiveFeedAdapter extends BaseAdapter implements ListAdapter {
        private List<Message> messages;
        private Context context;


        public LiveFeedAdapter(Context context, LiveFeedManager manager) {
            manager.addListener(new LiveFeedManager.NewMessagesListener() {
                @Override
                public void newMessagesAdded(List<Message> newMessages, List<Message> allMessages) {
                    messages = allMessages;
                    System.out.println(messages.size());
                    notifyDataSetChanged();
                }
            });
            this.messages = manager.messages;
            this.context = context;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            if (messages == null) return 0;
            else return messages.size();
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

            Message m = messages.get(position); // reverse the direction
            TextView message = (TextView) view.findViewById(R.id.live_feed_message);
            TextView timeSince = (TextView) view.findViewById(R.id.live_feed_time);

            message.setText(Html.fromHtml(m.message));
            message.setMovementMethod(LinkMovementMethod.getInstance()); // this is needed to make links work

            timeSince.setText(m.timeSince());

            return view;
        }
    }


}
