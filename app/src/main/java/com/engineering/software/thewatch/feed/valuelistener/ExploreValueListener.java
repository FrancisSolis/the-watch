package com.engineering.software.thewatch.feed.valuelistener;

import com.engineering.software.thewatch.feed.adapter.ExploreAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

/**
 * Author: King
 * Date: 3/16/2017
 */

public class ExploreValueListener implements ValueEventListener {

    private DatabaseReference postReference;
    private DatabaseReference userReference;

    private ExploreAdapter adapter;

    public ExploreValueListener(DatabaseReference postReference,
                                DatabaseReference userReference, ExploreAdapter adapter) {
        this.postReference = postReference;
        this.userReference = userReference;
        this.adapter = adapter;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null)
        {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                postReference.child(ds.getKey())
                    .addValueEventListener(
                        new ExplorePostValueListener(
                            postReference.child(ds.getKey()), userReference, adapter));
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
