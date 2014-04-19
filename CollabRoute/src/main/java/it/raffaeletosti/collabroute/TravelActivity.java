package it.raffaeletosti.collabroute;

import android.app.FragmentTransaction;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import it.raffaeletosti.collabroute.model.Travel;
import it.raffaeletosti.collabroute.model.UserHandler;


public class TravelActivity extends FragmentActivity {

    ViewPager mViewPager;
    MyPagerAdapter mViewPagerAdapter;
    protected Travel travel;
    static UserHandler user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel);
        String travelString = getIntent().getExtras().getString("travel");
        travel = new Travel();
        travel.createFromJSONString(travelString);
        user = getIntent().getParcelableExtra("user");
        final android.app.ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_TABS);
        mViewPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){

            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
            }
        });
        mViewPager.setAdapter(mViewPagerAdapter);
        android.app.ActionBar.TabListener tabListener = new android.app.ActionBar.TabListener() {
            @Override
            public void onTabSelected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {
                mViewPager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(android.app.ActionBar.Tab tab, FragmentTransaction ft) {

            }
        };
        for(int i = 0; i < 2; i++){
            actionBar.addTab(actionBar.newTab()
            .setText("TAB "+(i+1))
            .setTabListener(tabListener));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.travel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return GMapFragment.newInstance();
                default:
                    return RoutesFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
