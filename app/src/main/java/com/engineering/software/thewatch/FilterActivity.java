package com.engineering.software.thewatch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.engineering.software.thewatch.util.Timekeeper;
import com.engineering.software.thewatch.util.FeedFilters;
import com.engineering.software.thewatch.util.GlobalFilters;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FilterActivity extends AppCompatActivity {

    @Bind(R.id.f_naysayer)
    TextView naysayer;

    @Bind(R.id.f_watcher)
    TextView watcher;

    @Bind(R.id.f_spotter)
    TextView spotter;

    @Bind(R.id.f_scout)
    TextView scout;

    @Bind(R.id.f_spy)
    TextView spy;

    @Bind(R.id.f_chronicler)
    TextView chronicler;

    @Bind(R.id.f_maven)
    TextView maven;

    @Bind(R.id.f_sentinel)
    TextView sentinel;

    @Bind(R.id.f_distance)
    SeekBar sb_distance;

    @Bind(R.id.f_timestamp)
    SeekBar sb_timestamp;

    @Bind(R.id.tv_distance)
    TextView tv_distance;

    @Bind(R.id.tv_timestamp)
    TextView tv_timestamp;

    @Bind(R.id.f_public)
    TextView tv_public;

    @Bind(R.id.f_followed)
    TextView tv_followed;

    @Bind(R.id.btn_apply)
    Button btn_apply;
    Boolean publicity;

    int[] rank = {0,0,0,0,0,0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        ButterKnife.bind(this);
        publicity = true;

        switch (getIntent().getExtras().getInt("selectedTab")){
            case 0:
                getSupportActionBar().setTitle("Filter Watch Feed");
                break;
            case 1:
                getSupportActionBar().setTitle("Filter Map");
                break;
            case 2:
                getSupportActionBar().setTitle("Filter Explore");
                break;
        }


        sb_distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(progress);
                tv_distance.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sb_timestamp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress){
                    case 0:
                        tv_timestamp.setText("1 day");
                        break;
                    case 1:
                        tv_timestamp.setText("1 week");
                        break;
                    case 2:
                        tv_timestamp.setText("1 month");
                        break;
                    case 3:
                        tv_timestamp.setText("Limitless");
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tv_public.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!publicity){
                    publicity = true;
                    tv_public.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_followed.setTextColor(getResources().getColor(R.color.text_color));
                }
            }
        });

        tv_followed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(publicity){
                    publicity = false;
                    tv_followed.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_public.setTextColor(getResources().getColor(R.color.text_color));
                }
            }
        });

        View.OnClickListener rankListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               selectRank(Integer.parseInt(v.getTag().toString()));
            }
        };

        watcher.setOnClickListener(rankListener);
        spotter.setOnClickListener(rankListener);
        scout.setOnClickListener(rankListener);
        spy.setOnClickListener(rankListener);
        chronicler.setOnClickListener(rankListener);
        maven.setOnClickListener(rankListener);
        sentinel.setOnClickListener(rankListener);
        naysayer.setOnClickListener(rankListener);

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashSet<Integer> rankFilter = new HashSet<Integer>();
                for(int i = 0; i < rank.length; i++){
                    if(rank[i] == 1){
                        rankFilter.add(i-1);
                    }
                }

                long time = Timekeeper.DAY;
                switch (sb_timestamp.getProgress()){
                    case 0:
                        time = Timekeeper.DAY;
                        break;
                    case 1:
                        time = Timekeeper.WEEK;
                        break;
                    case 2:
                        time = Timekeeper.MONTH;
                        break;
                    case 3:
                        time = Timekeeper.NONE;
                        break;
                }

                switch (getIntent().getExtras().getInt("selectedTab")){
                    case 0:
                        GlobalFilters.watchFilter =
                                new FeedFilters(rankFilter, !publicity,
                                        sb_distance.getProgress(), time);
                        break;
                    case 1:
                        GlobalFilters.mapFilter =
                                new FeedFilters(rankFilter, !publicity,
                                        sb_distance.getProgress(), time);
                        break;
                    case 2:
                        GlobalFilters.exploreFilter =
                                new FeedFilters(rankFilter, !publicity, 0, time);
                        break;
                    default:
                        GlobalFilters.watchFilter =
                                new FeedFilters(rankFilter, !publicity,
                                        sb_distance.getProgress(), time);
                }


                Intent intent = new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        });


    }

    @Override
    protected void onResume() {
        initFilter(getIntent().getExtras().getInt("selectedTab"));
        super.onResume();

    }

    private void initRank(Set<Integer> rankList){
        for(Integer i: rankList){
            selectRank(i + 1);
        }
    }

    private void initFilter(int tab){
        int timestamp = 0;
        Log.d("filter", "loaded " + tab);
        switch (tab){
            case 0:
                initRank(GlobalFilters.watchFilter.ranks);
                if(GlobalFilters.watchFilter.isFollowing){
                    publicity = false;
                    tv_public.setTextColor(getResources().getColor(R.color.text_color));
                    tv_followed.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    publicity = true;
                    tv_public.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_followed.setTextColor(getResources().getColor(R.color.text_color));
                }
                sb_distance.setEnabled(true);
                sb_distance.setProgress((int) GlobalFilters.watchFilter.distance);

                if(GlobalFilters.watchFilter.time == Timekeeper.DAY)
                    timestamp = 0;
                else if(GlobalFilters.watchFilter.time == Timekeeper.WEEK)
                    timestamp = 1;
                else if(GlobalFilters.watchFilter.time == Timekeeper.MONTH)
                    timestamp = 2;
                else
                    timestamp = 3;

                sb_timestamp.setProgress(timestamp);
                sb_distance.invalidate();
                sb_timestamp.invalidate();
                break;
            case 1:
                initRank(GlobalFilters.mapFilter.ranks);
                if(GlobalFilters.mapFilter.isFollowing){
                    publicity = false;
                    tv_public.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_followed.setTextColor(getResources().getColor(R.color.text_color));
                }else{
                    publicity = true;
                    tv_public.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_followed.setTextColor(getResources().getColor(R.color.text_color));
                }
                sb_distance.setEnabled(false);
                if(GlobalFilters.watchFilter.time == Timekeeper.DAY)
                    timestamp = 0;
                else if(GlobalFilters.watchFilter.time == Timekeeper.WEEK)
                    timestamp = 1;
                else if(GlobalFilters.watchFilter.time == Timekeeper.MONTH)
                    timestamp = 2;
                else
                    timestamp = 3;

                sb_timestamp.setProgress(timestamp);
                sb_distance.invalidate();
                sb_timestamp.invalidate();
                break;
            case 2:
                initRank(GlobalFilters.exploreFilter.ranks);
                if(GlobalFilters.exploreFilter.isFollowing){
                    publicity = false;
                    tv_public.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_followed.setTextColor(getResources().getColor(R.color.text_color));
                }else{
                    publicity = true;
                    tv_public.setTextColor(getResources().getColor(R.color.colorPrimary));
                    tv_followed.setTextColor(getResources().getColor(R.color.text_color));
                }
                sb_distance.setEnabled(false);
                if(GlobalFilters.watchFilter.time == Timekeeper.DAY)
                    timestamp = 0;
                else if(GlobalFilters.watchFilter.time == Timekeeper.WEEK)
                    timestamp = 1;
                else if(GlobalFilters.watchFilter.time == Timekeeper.MONTH)
                    timestamp = 2;
                else
                    timestamp = 3;

                sb_timestamp.setProgress(timestamp);
                sb_distance.invalidate();
                sb_timestamp.invalidate();
                break;
            default:


        }
    }

    private void selectRank(int i){
        switch (i){
            case 0:
                if(rank[0] == 0){
                    rank[0] = 1;
                    naysayer.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[0] = 0;
                    naysayer.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
            case 1:
                if(rank[1] == 0){
                    rank[1] = 1;
                    watcher.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[1] = 0;
                    watcher.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
            case 2:
                if(rank[2] == 0){
                    rank[2] = 1;
                    spotter.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[2] = 0;
                    spotter.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
            case 3:
                if(rank[3] == 0){
                    rank[3] = 1;
                    scout.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[3] = 0;
                    scout.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
            case 4:
                if(rank[4] == 0){
                    rank[4] = 1;
                    spy.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[4] = 0;
                    spy.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
            case 5:
                if(rank[5] == 0){
                    rank[5] = 1;
                    chronicler.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[5] = 0;
                    chronicler.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
            case 6:
                if(rank[6] == 0){
                    rank[6] = 1;
                    maven.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[6] = 0;
                    maven.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
            case 7:
                if(rank[7] == 0){
                    rank[7] = 1;
                    sentinel.setTextColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    rank[7] = 0;
                    sentinel.setTextColor(getResources().getColor(R.color.text_color));
                }
                break;
        }
    }
}
