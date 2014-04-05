package it.digisin.collabroute;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import it.digisin.collabroute.connection.ConnectionHandler;
import it.digisin.collabroute.connection.TravelListHandler;
import it.digisin.collabroute.model.MeetingPoint;
import it.digisin.collabroute.model.Travel;
import it.digisin.collabroute.travel.TravelContent;


/**
 * An activity representing a single travel detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link travelListActivity}.
 * <p/>
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

    private enum ResponseMSG {OK, AUTH_FAILED, USER_NOT_CONFIRMED, EMAIL_NOT_FOUND, CONFIRM_MAIL_ERROR, DATABASE_ERROR, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR;}

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
        TravelListHandler route = new TravelListHandler(this, travel);
        route.execute("routes");
        travelDescription.setText(travel.getDescription());
        int adminId = travel.getAdmin().getId();
        String adminString = (adminId == travelListActivity.user.getId()) ? getString(R.string.travel_list_user_you) : travel.getAdmin().getName();
        travelAdministrator.setText(adminString);
        String users;
        if ((users = travel.getUsersName()) != null) {
            travelUsers.setText(getString(R.string.travel_list_user_you)+" "+users);
        } else {
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

    public void routesResponse(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(travelDetailActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(travelDetailActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(travelDetailActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(travelDetailActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(travelDetailActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(travelDetailActivity.this, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case AUTH_FAILED:
                    Toast.makeText(travelDetailActivity.this, ConnectionHandler.errors.get(ConnectionHandler.AUTH_FAILED), Toast.LENGTH_SHORT).show();
                    logOut();
                    return;
                case OK:
                    fillRoute(response.getJSONArray("array"));
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    public void logOut() {
        final Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, RESULT_OK);
        TravelContent.cleanList();
        travelListActivity.user = null;
        travelListActivity.travels = null;
        finish();
    }

    private void fillRoute(JSONArray response) {
        try {
            int len;
            String addresses = new String();
            if ((len = response.length()) > 0) {
                for (int i = 0; i < len; i++) {
                    JSONObject route = response.getJSONObject(i);
                    MeetingPoint mp = new MeetingPoint();
                    mp.setId(route.getInt("id"));
                    mp.setAddress(route.getString("address"));
                    addresses += mp.getAddress()+"\n";
                    mp.setLatitude(route.getString("latitude"));
                    mp.setLongitude(route.getString("longitude"));
                    travel.insertRoute(mp);
                }
                travelMP.setText(addresses);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}