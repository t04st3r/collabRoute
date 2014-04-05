package it.digisin.collabroute;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import it.digisin.collabroute.connection.ConnectionHandler;
import it.digisin.collabroute.connection.TravelListHandler;
import it.digisin.collabroute.model.Travel;
import it.digisin.collabroute.model.User;
import it.digisin.collabroute.model.UserHandler;
import it.digisin.collabroute.travel.TravelContent;


/**
 * An activity representing a list of travels. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link travelDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link travelListFragment} and the item details
 * (if present) is a {@link travelDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link travelListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class travelListActivity extends FragmentActivity
        implements travelListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private Dialog logoutDialog;
    public static UserHandler user;
    public static HashMap<String, Travel> travels;

    private enum ResponseMSG {OK, AUTH_FAILED, USER_NOT_CONFIRMED, EMAIL_NOT_FOUND, CONFIRM_MAIL_ERROR, DATABASE_ERROR, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(user == null) {
            user = getIntent().getParcelableExtra(LoginActivity.PARCELABLE_KEY);
        }
        setContentView(R.layout.activity_travel_list);
        if(travels == null) {
            TravelListHandler list = new TravelListHandler(this, user);
            list.execute("list");
        }
        if (findViewById(R.id.travel_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((travelListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.travel_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }


    /**
     * Callback method from {@link travelListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(travelDetailFragment.ARG_ITEM_ID, id);
            travelDetailFragment fragment = new travelDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.travel_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, travelDetailActivity.class);
            detailIntent.putExtra(travelDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.travel_list, menu);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            createLogOutDialog();
            logoutDialog.show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void createLogOutDialog() {
        if (logoutDialog == null) {
            logoutDialog = new Dialog(this);
            logoutDialog.setContentView(R.layout.logout_dialog);
            logoutDialog.setTitle(R.string.logout_title);
            final Button ok = (Button) logoutDialog.findViewById(R.id.Okbutton);
            final Button cancel = (Button) logoutDialog.findViewById(R.id.Cancelbutton);

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logOut();
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logoutDialog.dismiss();
                }
            });
        }
    }

    public void logOut() {
        final Intent intent = new Intent(this, LoginActivity.class);
        if (logoutDialog != null) {
            logoutDialog.dismiss();
        }
        startActivityForResult(intent, RESULT_OK);
        TravelContent.cleanList();
        user = null;
        travels = null;
        finish();
    }

    public void fillTravelList(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(travelListActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(travelListActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(travelListActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(travelListActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    Toast.makeText(travelListActivity.this, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(travelListActivity.this, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case AUTH_FAILED:
                    Toast.makeText(travelListActivity.this, ConnectionHandler.errors.get(ConnectionHandler.AUTH_FAILED), Toast.LENGTH_SHORT).show();
                    logOut();
                    return;
                case OK:
                    //System.err.println(response.toString()); debug
                    fillIt(response);
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private void fillIt(JSONObject response) {
        JSONArray arrayTravels = null;
        travels = new HashMap<String, Travel>();
        try {
            arrayTravels = response.getJSONArray("array");
            if (arrayTravels.length() == 0) {
                Toast.makeText(travelListActivity.this, this.getString(R.string.home_emptyList), Toast.LENGTH_LONG).show();
                return;
            }
            int length = arrayTravels.length(); //I feel dumb calling length() method on each iteration :P
            for (int i = 0; i < length; i++) {
                JSONObject item = arrayTravels.getJSONObject(i);
                Travel travel = new Travel();
                travel.setId(Integer.parseInt(item.getString("id")));
                travel.setName(item.getString("name"));
                travel.setDescription(item.getString("description"));
                if (!item.has("id_admin")) //if there isn't, the current user is the administrator
                    travel.setAdmin(user);
                else {
                    User admin = new User();
                    admin.setId(Integer.parseInt(item.getString("id_admin")));
                    admin.setName(item.getString("adm_name"));
                    travel.setAdmin(admin);
                }
                JSONArray users = item.getJSONArray("user");
                int userLength = users.length(); //same here
                for (int j = 0; j < userLength; j++) {
                    JSONObject jsonUser = users.getJSONObject(j);
                    User userFromArray = new User();
                    userFromArray.setName(jsonUser.getString("user_name"));
                    userFromArray.setId(Integer.parseInt(jsonUser.getString("user_id")));
                    travel.insertUser(userFromArray);
                }

                TravelContent.addItem(new TravelContent.TravelItem(String.valueOf(travel.getId()), travel.getName(), travel.getDescription()));
                travels.put(String.valueOf(travel.getId()) , travel);
            }
            travelListFragment.adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}