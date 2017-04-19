package com.engineering.software.thewatch.util;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

/**
 * Author: King
 * Date: 2/24/2017
 */

public class Timekeeper {
    private static DatabaseManager db = new DatabaseManager(null);

    public static long currentTime;

    public static long NONE = 0;
    public static long DAY = 86400000;
    public static long WEEK = DAY * 7;
    public static long MONTH = DAY * 30;

    public static void initializeTime() {
        Log.d("global", "update");

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    db.getRoot().child("timestamp").setValue(ServerValue.TIMESTAMP);
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("global", "update");
                }
            }
        }.start();

        db.getRoot().child("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentTime = (Long) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
