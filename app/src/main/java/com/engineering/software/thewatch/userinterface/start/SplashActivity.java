package com.engineering.software.thewatch.userinterface.start;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.userinterface.MainActivity;
import com.engineering.software.thewatch.util.GlobalFilters;
import com.engineering.software.thewatch.util.Timekeeper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        // initialize static utils
        Timekeeper.initializeTime();
        GlobalFilters.initializeFilters();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (user == null) {
                            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        } else
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));

                        finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);

            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);
    }
}
