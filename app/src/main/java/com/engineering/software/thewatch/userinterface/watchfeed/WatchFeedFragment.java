package com.engineering.software.thewatch.userinterface.watchfeed;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.engineering.software.thewatch.R;
import com.engineering.software.thewatch.feed.adapter.PostAdapter;
import com.engineering.software.thewatch.util.Timekeeper;
import com.engineering.software.thewatch.util.DatabaseManager;
import com.engineering.software.thewatch.util.FeedFilters;
import com.engineering.software.thewatch.util.GlobalFilters;
import com.engineering.software.thewatch.util.InterfaceManager;
import com.google.android.gms.location.LocationListener;

/**
 * Created by Francis on 17/02/2017.
 */

public class WatchFeedFragment extends Fragment implements LocationListener {
    public static final int REQUEST_CODE_RESOLUTION = 1;
    DatabaseManager dbm;
    InterfaceManager im;
    RecyclerView rv;
    LocationManager locationManager;
    String provider;
    Location location;

    ViewGroup mContainer;

    FeedFilters feedFilters;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.activity_watchfeed, container, false);
//
        mContainer = container;
        feedFilters = new FeedFilters();
//        googleApiClient = new GoogleApiClient.Builder(getContext())
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .build();
//        googleApiClient.connect();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        googleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();

        View mView = getView();

        im = new InterfaceManager();
        dbm = new DatabaseManager(mView.getContext());

        LinearLayoutManager llm = new LinearLayoutManager(mView.getContext());
        llm.scrollToPositionWithOffset(0, 0);

        rv = (RecyclerView) mView.findViewById(R.id.rv_watchfeed);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(llm);
        // Get the location manager
        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(provider);
        // Initialize the location fields

//        if (location != null) {
//            //FeedFilters filters = new FeedFilters();
//
////            filters.isFollowing = true;
//
//            Toast.makeText(getContext(),location.getLatitude() + ", " + location.getLongitude(),Toast.LENGTH_LONG).show();
//            PostAdapter adapter = im.watchFeedAdapter(
//                    dbm.geoQuery(location.getLatitude(),location.getLongitude(), feedFilters.distance), dbm.queryAllPosts(), feedFilters,Timekeeper.DAY);
//            rv.setAdapter(adapter);
//
//            setQueryListener(adapter);
//        } else {
//           location = null;
//            Toast.makeText(getContext(),"Location not available",Toast.LENGTH_LONG).show();
//        }

        if (location != null) {

            Toast.makeText(getContext(),location.getLatitude() + ", " + location.getLongitude(),Toast.LENGTH_LONG).show();

            PostAdapter adapter = im.watchFeedAdapter(
                    dbm.geoQuery(location.getLatitude(), location.getLongitude(),
                            GlobalFilters.watchFilter.distance), dbm.queryAllPosts(),
                    Timekeeper.DAY);

            rv.setAdapter(adapter);

            setQueryListener(adapter);
        } else {
            location = null;
            Toast.makeText(getContext(),"Location not available",Toast.LENGTH_LONG).show();
        }

    }

//    private LatLng getCurrentLocation() {
////        //Creating a location object
//        if (ActivityCompat.checkSelfPermission(getContext()
//                , android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            Log.d("tag","permission denied lol");
//            return null;
//        }
////        if (ActivityCompat.checkSelfPermission(getContext()
////                , android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
////                && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
////                != PackageManager.PERMISSION_GRANTED) {
////            // TODO: Consider calling
////            //    ActivityCompat#requestPermissions
////            // here to request the missing permissions, and then overriding
////            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
////            //                                          int[] grantResults)
////            // to handle the case where the user grants the permission. See the documentation
////            // for ActivityCompat#requestPermissions for more details.
////            return null;
////        }
//        Log.d("tag",googleApiClient.isConnected() + " <<<<<<<");
//        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//        if (location != null) {
//            //Getting longitude and latitude
//            LatLng loc = new LatLng(location.getLatitude() ,  location.getLongitude());
//
//            return  loc;
//        }
//        else {
//            Log.d("tag", "getlastloc null  ");
//            Log.d("tag",googleApiClient.isConnected() + "");
//            return null;
//        }
//    }

    public void setQueryListener(final PostAdapter adapter) {
        SearchView mSearchView = (SearchView)((CoordinatorLayout)(mContainer.getParent()))
                .findViewById(R.id.search);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("search query", newText);
                adapter.getFilter().filter(newText);
                rv.scrollToPosition(0);

                return true;
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void applyFilter(FeedFilters ff){
//        feedFilters = ff;
//
//        if (location != null) {
//           // Toast.makeText(getContext(),location.getLatitude() + ", " + location.getLongitude(),Toast.LENGTH_LONG).show();
//            PostAdapter adapter = im.watchFeedAdapter(
//                    dbm.geoQuery(location.getLatitude(),location.getLongitude(), ff.distance), dbm.queryAllPosts(), ff, ff.time);
//            rv.setAdapter(adapter);
//
//            setQueryListener(adapter);
//        } else {
//            location = null;
//            //Toast.makeText(getContext(),"Location not available",Toast.LENGTH_LONG).show();
//        }
    }
}
