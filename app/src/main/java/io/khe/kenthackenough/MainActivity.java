package io.khe.kenthackenough;

import android.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class MainActivity extends ActionBarActivity {
    public LiveFeedManager liveFeedManager;

    private String[] mViewTitles;
    private Fragment[] mViews = new Fragment[3];
    private DrawerLayout mViewDrawerLayout;
    private ListView mViewDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private int mCurrentView = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentView = savedInstanceState.getInt("active_view");
        }

        setContentView(R.layout.activity_dashboard);
        mViews[0] = new DashboardFragment();
        mViews[1] = new LiveFeedFragment();
        mViews[2] = new EventsFragment();

        mViewTitles = getResources().getStringArray(R.array.views);
        mViewDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mViewDrawerList = (ListView) findViewById(R.id.left_drawer);

        mViewDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.text_list_item, mViewTitles));
        mViewDrawerList.setOnItemClickListener(new ViewDrawerClickListener());

        selectView(mCurrentView);

        // todo add actual icons here built for purpose
        mDrawerToggle = new ActionBarDrawerToggle(this, mViewDrawerLayout, R.drawable.clover, R.drawable.clover);
        mViewDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // start the LiveFeedManager
        liveFeedManager = ((ApplicationWithStorage) getApplication()).liveFeedManager;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
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
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("active_view", mCurrentView);
    }

    private void selectView(int view) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mViews[view]).commit();
        mViewDrawerList.setItemChecked(view, true);
        setTitle(mViewTitles[view]);
        mViewDrawerLayout.closeDrawers();
        mCurrentView = view;
    }
    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    private class ViewDrawerClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectView(position);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DashboardFragment extends Fragment {

        public DashboardFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
            return rootView;
        }
    }

}
