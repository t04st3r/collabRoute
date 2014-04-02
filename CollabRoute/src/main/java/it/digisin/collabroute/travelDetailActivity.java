package it.digisin.collabroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;
import java.util.Arrays;

import it.digisin.collabroute.model.Travel;


/**
 * An activity representing a single travel detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link travelListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link travelDetailFragment}.
 */
public class travelDetailActivity extends FragmentActivity {


    TextView travelDescription;
    TextView travelAdministrator;
    TextView travelUsers;
    TextView travelMP;
    Travel travel;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_detail);
        id = getIntent().getStringExtra(travelDetailFragment.ARG_ITEM_ID);

       travelDescription = (TextView) findViewById(R.id.travelDetailActDescription);
        travelAdministrator = (TextView) findViewById(R.id.travelDetailActAdmin);
        travelUsers = (TextView) findViewById(R.id.travelDetailActUser);
        travelMP = (TextView) findViewById(R.id.travelDetailActRoutes);
        travel = travelListActivity.travels.get(id);
        travelDescription.setText(travel.getDescription());
        travelAdministrator.setText(travel.getAdmin().getName());
        String users;
        if((users = travel.getUsersName()) != null){
            travelUsers.setText(users);
        }else{
           travelUsers.setText(getString(R.string.travel_users_emptyArray));
        }



        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(travelDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(travelDetailFragment.ARG_ITEM_ID));
            travelDetailFragment fragment = new travelDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.travel_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, travelListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
