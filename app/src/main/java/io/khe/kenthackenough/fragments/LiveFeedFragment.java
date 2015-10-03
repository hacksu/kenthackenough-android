package io.khe.kenthackenough.fragments;


import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.khe.kenthackenough.FriendlyTimeSince;
import io.khe.kenthackenough.KHEApp;
import io.khe.kenthackenough.R;
import io.khe.kenthackenough.backend.Messages.LiveFeedManager;
import io.khe.kenthackenough.backend.Messages.Message;


/**
 * Shows a list of messages from the hackathond
 */
public class LiveFeedFragment extends Fragment {
    public static boolean mActive = false;
    NotificationManager notificationManager;
    private SwipeRefreshLayout refresh;
    private LiveFeedManager liveFeedManager;

    public LiveFeedFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        notificationManager.cancelAll();
        //mActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        //mActive = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_live_feed, container, false);
        RecyclerView messages = (RecyclerView) view.findViewById(R.id.messages);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        messages.setLayoutManager(llm);

        liveFeedManager = ((KHEApp) getActivity().getApplication()).liveFeedManager;
        refresh = ((SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh));

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                liveFeedManager.halt();
                liveFeedManager.start();
            }
        });
        liveFeedManager.addNewMessagesListener(new LiveFeedManager.NewMessagesListener() {
            @Override
            public void newMessagesAdded(List<Message> newMessages, List<Message> allMessages) {
                refresh.setRefreshing(false);

            }
        });
        liveFeedManager.addErrorMessageListener(new LiveFeedManager.ErrorMessageListener() {
            @Override
            public void error(Object error) {
                refresh.setRefreshing(false);
                Toast.makeText(getActivity(), "Error Fetching Updates", Toast.LENGTH_SHORT).show();
            }
        });

        messages.setAdapter(new LiveFeedAdapter(liveFeedManager));
        return view;
    }

    private class LiveFeedAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private List<Message> messages;


        public LiveFeedAdapter( LiveFeedManager manager) {
            manager.addNewMessagesListener(new LiveFeedManager.NewMessagesListener() {
                @Override
                public void newMessagesAdded(List<Message> newMessages, List<Message> allMessages) {
                    messages = allMessages;
                    notifyDataSetChanged();
                }
            });
            manager.addDeleteMessageListener(new LiveFeedManager.DeletedMessageListener() {
                @Override
                public void messageDeleted(Message deleted, List<Message> allMessages) {
                    messages = allMessages;
                    notifyDataSetChanged();
                }
            });
            manager.addUpdateMessageListener(new LiveFeedManager.UpdatedMessageListener() {
                @Override
                public void messageUpdated(Message updated, List<Message> allMessages) {
                    messages = allMessages;
                    notifyDataSetChanged();
                }
            });
            this.messages = manager.messages;
            notifyDataSetChanged();
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.live_feed_item, viewGroup, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder messageViewHolder, int i) {
            Message m = messages.get(i);

            messageViewHolder.message.setText(m.getFormatted());
            messageViewHolder.message.setMovementMethod(LinkMovementMethod.getInstance());
            messageViewHolder.timeSince.setTime(m.getCreated().getTime());
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {
        protected TextView message;
        protected FriendlyTimeSince timeSince;


        public MessageViewHolder(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.live_feed_message);
            timeSince = (FriendlyTimeSince) itemView.findViewById(R.id.live_feed_time);
        }
    }
}
