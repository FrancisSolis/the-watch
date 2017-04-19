package com.engineering.software.thewatch.userinterface.map;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.map.CustomRenderer;
import com.engineering.software.thewatch.model.db.Post;
import com.engineering.software.thewatch.model.feed.PostItem;
import com.engineering.software.thewatch.model.db.User;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.GlobalFilters;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Created by JKMT on 02/24/2017.
 */

public class WatchMapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private ClusterManager<PostItem> mClusterManager;
    private GoogleMap mMap;
    public double longitude;
    public double latitude;
    private GoogleApiClient googleApiClient;

    private DatabaseManager dm;

    private static View view;

    SupportMapFragment mMapView;

    private ValueEventListener locationListener;
    private Map<String, ValueEventListener> postListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.activity_map, container, false);
            setUpMap(savedInstanceState);
            googleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        } catch (InflateException e) {

        }
        dm = new DatabaseManager(null);

        postListener = new HashMap<>();

        return view;
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        disposeListeners();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposeListeners();
        try {
            Fragment fragment = (getFragmentManager().findFragmentById(R.id.w_map));
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.remove(fragment);
            ft.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (mMap != null) {
            //readItems();
            //mClusterManager.notifyAll();
            Log.d("map", "map not null");
            return;
        }
        mMap = map;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_map));

            if (!success) {
                Log.e("tag", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("tag", "Can't find style. Error: ", e);
        }

        startMap();
    }

    @Override
    public void onConnected(Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getCurrentLocation() {
        mMap.clear();
        //Creating a location object
        if (ActivityCompat.checkSelfPermission(getContext()
                , android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            //moving the map to location
            moveMap();
        }
    }

    private void moveMap() {
        //String to display current latitude and longitude
        String msg = latitude + ", " + longitude;

        //Creating a LatLng Object to store Coordinates
        LatLng latLng = new LatLng(latitude, longitude);


        //Adding marker to map


        //Moving the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Animating the camera
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        getMap().getUiSettings().setZoomControlsEnabled(true);
        getMap().getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        getMap().setMyLocationEnabled(true);
        //Displaying current coordinates in toast
    }

    private void setUpMap(Bundle savedInstanceState) {
        mMapView = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.w_map));
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    private void startMap() {
        mClusterManager = new ClusterManager<>(getContext(), getMap());
        mClusterManager.setRenderer(new CustomRenderer(getContext(), getMap(), mClusterManager));
        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<PostItem>() {
            @Override
            public void onClusterItemInfoWindowClick(PostItem postItem) {
                Intent i = new Intent(WatchMapFragment.this.getActivity(), MapPostActivity.class);
                i.putExtra("postID", postItem.postID);
                WatchMapFragment.this.getActivity().startActivity(i);
            }
        });
        getMap().setOnCameraIdleListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);

        readItems();

    }

    private void readItems() {
        final DatabaseReference root = dm.getRoot();

        final Set<PostItem> posts = new HashSet<>();

        locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    //int i = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        final PostItem post = new PostItem();
                        post.postID = ds.getKey();

                        LatLng loc = new LatLng(
                                (double) ds.child("l").child("0").getValue(),
                                (double) ds.child("l").child("1").getValue());

                        post.mPosition = loc;

                        ValueEventListener aPostListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Post postModel = dataSnapshot.getValue(Post.class);

                                if (postModel != null
                                        && GlobalFilters.mapFilter.filter(postModel)) {
                                    post.mTitle = postModel.title;
                                    post.mSnippet = postModel.description;

                                    root.child("user").child(postModel.userID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User user = dataSnapshot.getValue(User.class);

                                            if (GlobalFilters.mapFilter.filter(user))
                                                mClusterManager.addItem(post);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                                //mClusterManager.addItem(post);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };

                        root.child("post").child(post.postID).addValueEventListener(aPostListener);
                        postListener.put(post.postID, aPostListener);

                        posts.add(post);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        root.child("geofire").addValueEventListener(locationListener);
    }

    private void disposeListeners() {
        if (dm != null) {
            final DatabaseReference root = dm.getRoot();
            if (locationListener != null) {
                root.child("geofire").removeEventListener(locationListener);

                for (String postID : postListener.keySet())
                    root.child("post").child(postID).removeEventListener(postListener.get(postID));
            }
        }
    }

    public void moveMapCamera(LatLng latlng) {
        getMap().moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }

    private GoogleMap getMap() {
        return mMap;
    }
}
