package com.engineering.software.thewatch.feed.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.engineering.software.thewatch.feed.viewholder.ExploreViewHolder;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: King
 * Date: 3/16/2017
 */

public class ExploreAdapter
        extends RecyclerView.Adapter<ExploreViewHolder>
        implements Filterable {

    private DatabaseManager db;

    public List<PostInformation> posts;
    public List<PostInformation> filteredPosts;

    private boolean noQuery;

    public ExploreAdapter(DatabaseManager db, List<PostInformation> posts) {
        this.db = db;
        this.posts = posts;

        filteredPosts = new ArrayList<>(posts);

        noQuery = true;
    }

    @Override
    public ExploreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.explore_card, parent, false);

        return new ExploreViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ExploreViewHolder holder, int position) {

        PostInformation postInformation;
        if (noQuery)
            postInformation = posts.get(position);
        else
            postInformation = filteredPosts.get(position);

        holder.setView(postInformation);
        holder.setViewListener(postInformation.post.postID);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        return effectivePosts().size();
    }

    @Override
    public Filter getFilter() {
        return new ExploreFilter(this, posts);
    }

    public List<PostInformation> effectivePosts() {
        if (noQuery)
            return posts;
        else
            return filteredPosts;
    }

    private static class ExploreFilter extends Filter {

        final ExploreAdapter adapter;

        final List<PostInformation> originalPosts;
        final List<PostInformation> filteredPosts;

        public ExploreFilter(ExploreAdapter adapter, List<PostInformation> originalPosts) {
            this.adapter = adapter;
            this.originalPosts = new ArrayList<>(originalPosts);
            this.filteredPosts = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredPosts.clear();
            final FilterResults results = new FilterResults();

            if (constraint.length() == 0) {
                filteredPosts.addAll(originalPosts);
            } else { // Filter logic
                final String filterPattern = "(?i).*" + constraint.toString().trim() + ".*";

                for (final PostInformation postInformation : originalPosts) {
                    if (postInformation.post.title.matches(filterPattern)) {
                        filteredPosts.add(postInformation);
                    } else if (postInformation.post.description.matches(filterPattern)) {
                        filteredPosts.add(postInformation);
                    } else if (postInformation.post.mood.matches(filterPattern)) {
                        filteredPosts.add(postInformation);
                    } else if (postInformation.user != null && postInformation.user.displayName.matches(filterPattern)) {
                        filteredPosts.add(postInformation);
                    } else if (postInformation.post.tags != null) {
                        for (String tag : postInformation.post.tags.keySet()) {
                            if (tag.matches(filterPattern)) {
                                filteredPosts.add(postInformation);
                                break;
                            }
                        }
                    } else if (postInformation.post.placePrimary.matches(filterPattern)
                            || postInformation.post.placeSecondary.matches(filterPattern)) {
                        filteredPosts.add(postInformation);
                    }
                }
            }
            results.values = filteredPosts;
            results.count = filteredPosts.size();
            adapter.noQuery = false;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (adapter.filteredPosts != null && results.values != null) {
                adapter.filteredPosts.clear();
                adapter.filteredPosts.addAll((ArrayList<PostInformation>) results.values);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
