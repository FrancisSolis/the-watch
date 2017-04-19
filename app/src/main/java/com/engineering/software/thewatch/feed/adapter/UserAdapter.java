package com.engineering.software.thewatch.feed.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.feed.viewholder.UserViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author: King
 * Date: 3/16/2017
 */

public class UserAdapter
        extends RecyclerView.Adapter<UserViewHolder>
        implements Filterable {

    public Map<String, String> map;
    public List<String> userNames;
    public List<String> filteredUserNames;

    private boolean noQuery;

    public UserAdapter(Map<String, String> map) {
        this.map = map;
        userNames = new ArrayList<>();
        filteredUserNames = new ArrayList<>();

        noQuery = true;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_card, parent, false);

        return new UserViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        String displayName;

        if (noQuery)
            displayName = userNames.get(position);
        else
            displayName = filteredUserNames.get(position);

        holder.setName(displayName);
        holder.setOnClick(map.get(displayName));
    }

    @Override
    public int getItemCount() {
        if (noQuery)
            return userNames.size();
        else
            return filteredUserNames.size();
    }

    @Override
    public Filter getFilter() {
        return new UserFilter(this, map);
    }

    public List<String> effectivePosts() {
        if (noQuery)
            return userNames;
        else
            return filteredUserNames;
    }

    public static class UserFilter
            extends Filter {

        private UserAdapter adapter;

        private List<String> originalUserNames;
        private List<String> filteredUserNames;

        public UserFilter(UserAdapter adapter, Map<String, String> map) {
            this.adapter = adapter;

            originalUserNames = new ArrayList<>(map.keySet());
            filteredUserNames = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredUserNames.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredUserNames.addAll(originalUserNames);
            } else { // Filter logic
                final String filterPattern = "(?i).*" + constraint.toString().trim() + ".*";

                for (final String string : originalUserNames) {
                    if (string.matches(filterPattern)) {
                        filteredUserNames.add(string);
                    }
                }
            }
            results.values = filteredUserNames;
            results.count = filteredUserNames.size();
            adapter.noQuery = false;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (adapter.filteredUserNames != null && results.values != null) {
                adapter.filteredUserNames.clear();
                adapter.filteredUserNames.addAll((ArrayList<String>) results.values);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
