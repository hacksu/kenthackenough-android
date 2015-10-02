package io.khe.kenthackenough;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import io.khe.kenthackenough.backend.LiveFeedManager;
import io.khe.kenthackenough.fragments.AboutFragment;
import io.khe.kenthackenough.fragments.DashboardFragment;
import io.khe.kenthackenough.fragments.EventsFragment;
import io.khe.kenthackenough.fragments.LiveFeedFragment;


public class MainActivity extends AppCompatActivity {
    public LiveFeedManager liveFeedManager;

    private String[] mViewTitles;
    ViewPager viewPager;

    private int mCurrentView = 0;
    private TabLayout tabBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentView = savedInstanceState.getInt("active_view");
        }

        setContentView(R.layout.activity_main);


        mViewTitles = getResources().getStringArray(R.array.views);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Adapter adapter = new Adapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);



        tabBar = (TabLayout) findViewById(R.id.tab_bar);

        for(String title: mViewTitles) {
            TabLayout.Tab newTab = tabBar.newTab();
            newTab.setText(title);
            tabBar.addTab(newTab);
        }

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabBar));

        // Switch to the saved view
        mCurrentView = getIntent().getIntExtra("view", mCurrentView);
        Log.i(Config.DEBUG_TAG, "main activity loaded and switching to " + mCurrentView);
        selectView(mCurrentView);
        viewPager.setCurrentItem(mCurrentView);

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
        viewPager.setCurrentItem(view);
        setTitle(mViewTitles[view]);


        mCurrentView = view;
    }
    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }


    public static class Adapter extends FragmentPagerAdapter{

        Fragment[] views  = new Fragment[4];

        public Adapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            views[0] = new DashboardFragment();
            views[1] = new LiveFeedFragment();
            views[2] = new EventsFragment();
            views[3] = new AboutFragment();
        }

        @Override
        public Fragment getItem(int position) {
            return views[position];
        }

        @Override
        public int getCount() {
            return views.length;
        }
    }

}
