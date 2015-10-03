package io.khe.kenthackenough;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
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

    private static String[] mViewTitles;
    private ViewPager mViewPager;
    private TabLayout mTabBar;
    private AppBarLayout mAppBar;

    private int mCurrentView = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentView = savedInstanceState.getInt("active_view");
        }

        setContentView(R.layout.activity_main);


        mViewTitles = getResources().getStringArray(R.array.views);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabBar = (TabLayout) findViewById(R.id.tab_bar);
        mAppBar = (AppBarLayout) findViewById(R.id.appBarLayout);

        setSupportActionBar(toolbar);

        Adapter adapter = new Adapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(4);



        mTabBar.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Switch to the saved view
        mCurrentView = getIntent().getIntExtra("view", mCurrentView);
        Log.i(Config.DEBUG_TAG, "main activity loaded and switching to " + mCurrentView);
        selectView(mCurrentView);
        mViewPager.setCurrentItem(mCurrentView);

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
        setTitle(mViewTitles[view]);

        if(view == 0 || view == 3) {
            mAppBar.setExpanded(true);
        }
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
            views[1] = new EventsFragment();
            views[2] = new LiveFeedFragment();
            views[3] = new AboutFragment();
        }

        @Override
        public Fragment getItem(int position) {
            return views[position];
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mViewTitles[position];
        }

        @Override
        public int getCount() {
            return views.length;
        }
    }

}
