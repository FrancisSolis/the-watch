package com.engineering.software.thewatch.userinterface;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.engineering.software.thewatch.FilterActivity;
import com.engineering.software.thewatch.PostActivity;
import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.userinterface.map.WatchMapFragment;
import com.engineering.software.thewatch.userinterface.explore.ExploreFragment;
import com.engineering.software.thewatch.userinterface.profile.ProfileFragment;
import com.engineering.software.thewatch.userinterface.profile.ProfileSearchActivity;
import com.engineering.software.thewatch.userinterface.profile.ProfileSetupActivity;
import com.engineering.software.thewatch.userinterface.start.LoginActivity;
import com.engineering.software.thewatch.userinterface.watchfeed.WatchFeedFragment;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 3;

    public final static int POST_REQUEST_CODE = 1;
    public final static int FILTER_REQUEST_CODE = 2;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private TabLayout tabLayout;

    GoogleApiClient mGoogleApiClient;
    GoogleSignInOptions gso;

    int tabUnselected[] = {
            R.drawable.home_0,
            R.drawable.map_0,
            R.drawable.explore_0,
            R.drawable.user_0,
    };

    int tabSelected[] = {
            R.drawable.home_1,
            R.drawable.map_1,
            R.drawable.explore_1,
            R.drawable.user_1,
    };

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    final DatabaseManager dm = new DatabaseManager(MainActivity.this);
                    dm.getRoot().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("tag", "Data Change");
                            if (!dataSnapshot.hasChild(user.getUid())) {
                                startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class));
                                Log.d("tag", "User Setup start");
                                //mGoogleApiClient.reconnect();
                                finish();
                            } else {
//                                //Toast.makeText(LoginActivity.this, "Error i", Toast.LENGTH_LONG).show();
//                                startActivity(new Intent(MainActivity.this,MainActivity.class));
//                                //mGoogleApiClient.reconnect();
//                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        };

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {

                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_LONG).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //Set up icons for the tabs
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(tabUnselected[i]);
        }

        //Set up listener for tab select
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.setIcon(tabSelected[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.setIcon(tabUnselected[tab.getPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                tab.setIcon(tabSelected[tab.getPosition()]);
                tabLayout.setScrollPosition(tab.getPosition(),0f,true);
            }
        });
        tabLayout.getTabAt(1).select();
        tabLayout.getTabAt(0).select();

        mSearchView = ((SearchView) findViewById(R.id.search));

        mSearchView.setFocusable(false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, PostActivity.class),POST_REQUEST_CODE);
            }
        });

        WatchFeedFragment feedFragment = (WatchFeedFragment) mSectionsPagerAdapter.getItem(0);

//        ((SearchView) findViewById(R.id.search)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("tag", "onclick searchview");
//                if(tabLayout.getSelectedTabPosition() == 1){
//                    try {
//                        Intent intent =
//                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
//                                        .build(MainActivity.this);
//                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
//                    } catch (GooglePlayServicesRepairableException e) {
//                        // TODO: Handle the error.
//                    } catch (GooglePlayServicesNotAvailableException e) {
//                        // TODO: Handle the error.
//                    }
//                }
//            }
//        });

//        ((SearchView) findViewById(R.id.search)).setOnSearchClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("tag", "onclick searchview");
//                if(tabLayout.getSelectedTabPosition() == 1){
//                    try {
//                        Intent intent =
//                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
//                                        .build(MainActivity.this);
//                        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
//                    } catch (GooglePlayServicesRepairableException e) {
//                        // TODO: Handle the error.
//                    } catch (GooglePlayServicesNotAvailableException e) {
//                        // TODO: Handle the error.
//                    }
//                }
//            }
//
//        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                Log.d("tag", "onclick searchview");
                if(hasFocus) {
                    if (tabLayout.getSelectedTabPosition() == 1) {
                        try {
                            Intent intent =
                                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                            .build(MainActivity.this);
                            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                        } catch (GooglePlayServicesRepairableException e) {
                            // TODO: Handle the error.
                        } catch (GooglePlayServicesNotAvailableException e) {
                            // TODO: Handle the error.
                        }
                    } else if(tabLayout.getSelectedTabPosition() == 3){
                        MainActivity.this.startActivity(new Intent(MainActivity.this,ProfileSearchActivity.class));
                    }
                }
            }
        });

//        ((SearchView) findViewById(R.id.search))
        SearchView.SearchAutoComplete autoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);




    }

    @Override
    protected void onResume() {
        mSearchView.clearFocus();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.signOut) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            FirebaseAuth.getInstance().signOut();
                        }
                    });
                }
            })
                    .setCancelable(true)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();

        } else if(id == R.id.filter){
            if(tabLayout.getSelectedTabPosition() != 3) {
                Intent i = new Intent(this, FilterActivity.class);
                i.putExtra("selectedTab", tabLayout.getSelectedTabPosition());

                startActivityForResult(i, FILTER_REQUEST_CODE);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == POST_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                tabLayout.setScrollPosition(0,0f,true);
                mViewPager.setCurrentItem(0);
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            mSearchView.clearFocus();
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                WatchMapFragment watchMapFragment = (WatchMapFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mViewPager.getCurrentItem());
                watchMapFragment.moveMapCamera(place.getLatLng());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Toast.makeText(this,status.toString(),Toast.LENGTH_LONG).show();

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int pos = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView;
            switch (pos) {
//                case 0:
//                    break;
//                case 1:
//                case 2:
                case 3:
                    rootView = inflater.inflate(R.layout.activity_profile, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);

            }

//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            Log.d("mytag", "pos >>>>>>>> " + position);
            switch (position) {
                case 0:
                    return new WatchFeedFragment();
                case 1:
                    return new WatchMapFragment();
                case 2:
                    return new ExploreFragment();
                case 3:
                    return new ProfileFragment();
                default:
                    return  PlaceholderFragment.newInstance(1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}
