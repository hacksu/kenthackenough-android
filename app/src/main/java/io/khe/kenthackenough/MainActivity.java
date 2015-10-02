package io.khe.kenthackenough;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import io.khe.kenthackenough.backend.LiveFeedManager;
import io.khe.kenthackenough.fragments.AboutFragment;
import io.khe.kenthackenough.fragments.DashboardFragment;
import io.khe.kenthackenough.fragments.EventsFragment;
import io.khe.kenthackenough.fragments.LiveFeedFragment;


public class MainActivity extends ActionBarActivity {
    public LiveFeedManager liveFeedManager;

    private String[] mViewTitles;
    private Fragment[] mViews;

    private int mCurrentView = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViews = getPanes();


        if (savedInstanceState != null) {
            mCurrentView = savedInstanceState.getInt("active_view");
        }

        setContentView(R.layout.activity_main);


        mViewTitles = getResources().getStringArray(R.array.views);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        mCurrentView = intent.getIntExtra("view", mCurrentView);





        Log.i(Config.DEBUG_TAG, "main activity loaded and switching to " + mCurrentView);

        TabLayout tabBar = (TabLayout) findViewById(R.id.tab_bar);

        for(String title: mViewTitles) {
            TabLayout.Tab newTab = tabBar.newTab();
            newTab.setText(title);
            tabBar.addTab(newTab);
        }

        selectView(mCurrentView);
        tabBar.getTabAt(mCurrentView).select();

        tabBar.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectView(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                selectView(tab.getPosition());
            }
        });

        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private Fragment[] getPanes(){
        Fragment[] views  = new Fragment[4];
        views[0] = new DashboardFragment();
        views[1] = new LiveFeedFragment();
        views[2] = new EventsFragment();
        views[3] = new AboutFragment();

        return views;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("active_view", mCurrentView);
    }

    private void selectView(int view) {
        FragmentManager fragmentManager = getFragmentManager();

        Log.d(Config.DEBUG_TAG, "Set fragment to " + view + " so fragment " + mViews[view].getClass());

        fragmentManager.beginTransaction()
                .replace(R.id.container, mViews[view])
                .commit();

        setTitle(mViewTitles[view]);

        mCurrentView = view;
    }
    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

}
