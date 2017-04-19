package com.engineering.software.thewatch.feed.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.model.feed.PostInformation;
import com.engineering.software.thewatch.feed.viewholder.PostViewHolder;
import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.model.db.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: King
 * Date: 2/19/2017
 */

public class PostAdapter
        extends RecyclerView.Adapter<PostViewHolder>
        implements Filterable {

    private DatabaseManager db;

    public List<PostInformation> posts;
    public List<PostInformation> filteredPosts;

    boolean noQuery;
    long timeLimit;

    public PostAdapter(DatabaseManager db, List<PostInformation> posts, long timeLimit) {
        this.db = db;
        this.posts = posts;
        this.timeLimit = timeLimit;

        filteredPosts = new ArrayList<>(posts);

        noQuery = true;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_card_propose, parent, false);

        return new PostViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final PostViewHolder holder, int position) {
        Log.d("loc_prob", filteredPosts.size() + " << filtered size");
        Log.d("loc_prob", posts.size() + " << post size");
        Log.d("loc_prob", noQuery + " << noQuery");

        PostInformation postInformation;
        if (noQuery)
            postInformation = posts.get(position);
        else
            postInformation = filteredPosts.get(position);

        holder.setPostData(postInformation.post);
        holder.setVoteListeners(postInformation.post.postID);
        db.getRoot().child("user").child(postInformation.post.userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null)
                            holder.setUserData(user);
                        holder.setProfileListener(user.id);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        if (noQuery)
            return posts.size();
        else
            return filteredPosts.size();
    }

    @Override
    public Filter getFilter() {
//        if (postFilter == null)
//            postFilter = new PostFilter(this, posts, feedFilters);
//        return postFilter;
        return new PostFilter(this, posts);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public List<PostInformation> effectivePosts() {
        if (noQuery)
            return posts;
        else
            return filteredPosts;
    }

    private static class PostFilter extends Filter {

        final PostAdapter adapter;

        final List<PostInformation> originalPosts;

        final ArrayList<PostInformation> filteredPosts;

        private PostFilter(PostAdapter adapter, List<PostInformation> originalPosts) {
            super();
            this.adapter = adapter;
            this.originalPosts = new ArrayList<>(originalPosts);
            this.filteredPosts = new ArrayList<>();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            filteredPosts.clear();
            final FilterResults results = new FilterResults();

//            List<PostInformation> tempFilter = new ArrayList<>();
//            String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            Log.d("myadapter", "about to filter");

            // FILTER
//            if (feedFilters == null) {
//                Log.d("myadapter", "filter is null");
//                tempFilter.addAll(originalPosts);
//            } else {
//                Log.d("myadapter", "else");
//
//                for (final PostInformation postInformation : originalPosts) {
//                    Log.d("myadapter", postInformation.user.displayName + " " + (postInformation.user == null ? "null" : "not null"));
//                    Log.d("myadapter", postInformation.user.displayName + " " + (postInformation.user.follower == null ? "null" : "not null"));
//                    if ((feedFilters.ranks.size() == 0
//                            || feedFilters.ranks.size() == 8
//                            || (postInformation.user != null
//                            && feedFilters.ranks.contains(postInformation.user.rank)))
//                            && (postInformation.user != null
//                            && (feedFilters.isFollowing
//                            == (postInformation.user.follower != null
//                            && postInformation.user.follower.keySet().contains(myID))))) {
//                        tempFilter.add(postInformation);
//                    }
//                }
//            }

            // SEARCH

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
