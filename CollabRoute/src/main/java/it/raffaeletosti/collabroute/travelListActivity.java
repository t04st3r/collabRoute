package it.raffaeletosti.collabroute;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.EmailValidator;
import it.raffaeletosti.collabroute.connection.TravelListHandler;
import it.raffaeletosti.collabroute.model.Travel;
import it.raffaeletosti.collabroute.model.User;
import it.raffaeletosti.collabroute.model.UserHandler;
import it.raffaeletosti.collabroute.travels.TravelContent;
import it.raffaeletosti.collabroute.users.CustomArrayAdapterUserList;
import it.raffaeletosti.collabroute.users.UserContent;

import static android.R.layout.simple_dropdown_item_1line;


/**
 * An activity representing a list of travels. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link travelDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link it.raffaeletosti.collabroute.travelListFragment} and the item details
 * (if present) is a {@link it.raffaeletosti.collabroute.travelDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link it.raffaeletosti.collabroute.travelListFragment.Callbacks} interface
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
    private Dialog newTravelDialog;
    public static UserHandler user;
    public static HashMap<String, Travel> travels;
    public static HashMap<String, User> users;
    public static ArrayAdapter adapter;
    public static ListView usersList;
    private AutoCompleteTextView autoCompleteUsers;
    private EditText travelName;
    private EditText travelDescription;
    private Travel newTravel;

    private enum ResponseMSG {OK, AUTH_FAILED, DATABASE_ERROR, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (user == null) {
            user = getIntent().getParcelableExtra(LoginActivity.PARCELABLE_KEY);
        }
        setContentView(R.layout.activity_travel_list);
        if (travels == null) {
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
    }


    /**
     * Callback method from {@link it.raffaeletosti.collabroute.travelListFragment.Callbacks}
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
        if (id == R.id.action_createTravel) {
            createNewTravelDialog();
            newTravelDialog.show();
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
        users = null;
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
                    fillIt(response);
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private void fillIt(JSONObject response) {
        JSONArray arrayTravels;
        travels = new HashMap<String, Travel>();
        try {
            arrayTravels = response.getJSONArray("array");
            if (arrayTravels.length() == 0) {
                Toast.makeText(travelListActivity.this, this.getString(R.string.home_emptyList), Toast.LENGTH_LONG).show();
            }else {
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
                        admin.setEMail(item.getString("adm_mail"));
                        travel.setAdmin(admin);
                    }
                    JSONArray users = item.getJSONArray("user");
                    int userLength = users.length(); //same here
                    for (int j = 0; j < userLength; j++) {
                        JSONObject jsonUser = users.getJSONObject(j);
                        User userFromArray = new User();
                        userFromArray.setName(jsonUser.getString("user_name"));
                        userFromArray.setId(Integer.parseInt(jsonUser.getString("user_id")));
                        userFromArray.setEMail(jsonUser.getString("user_email"));
                        travel.insertUser(userFromArray);
                    }
                    TravelContent.addItem(new TravelContent.TravelItem(String.valueOf(travel.getId()), travel.getName(), travel.getDescription()));
                    travels.put(String.valueOf(travel.getId()), travel);
                }
            }
            JSONArray usersList = response.getJSONArray("users");
            int length = usersList.length();
            users = new HashMap<String, User>();
            for (int i = 0; i < length; i++) {
                JSONObject item = usersList.getJSONObject(i);
                User userForList = new User();
                userForList.setId(item.getInt("id"));
                userForList.setName(item.getString("name"));
                userForList.setEMail(item.getString("email"));
                users.put(String.valueOf(userForList.getId()), userForList);
            }
            travelListFragment.adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createNewTravelDialog() {
        if (newTravelDialog == null) {
            newTravelDialog = new Dialog(this);
            newTravelDialog.setContentView(R.layout.new_travel_dialog);
            newTravelDialog.setTitle(R.string.new_travel_dialog_title);
            newTravelDialog.setCancelable(false);
            newTravelDialog.setCanceledOnTouchOutside(false);
            final Button addUser = (Button) newTravelDialog.findViewById(R.id.addUserButton);
            final Button deleteUser = (Button) newTravelDialog.findViewById(R.id.deleteUserbutton);
            final Button addTravel = (Button) newTravelDialog.findViewById(R.id.confirmTravelButton);
            final Button cancelTravel = (Button) newTravelDialog.findViewById(R.id.deleteTravelButton);
            travelName = (EditText) newTravelDialog.findViewById(R.id.newTravelName);
            travelDescription = (EditText) newTravelDialog.findViewById(R.id.newTravelDescription);
            usersList = (ListView) newTravelDialog.findViewById(R.id.addedUsersListView);
            usersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            adapter = new CustomArrayAdapterUserList(this, R.layout.userlistview_row, UserContent.ITEMS);
            usersList.setAdapter(adapter);
            autoCompleteUsers = (AutoCompleteTextView)
                    newTravelDialog.findViewById(R.id.autoCompleteTextViewUsers);
            autoCompleteUsers.setThreshold(1);
            ArrayAdapter<String> userAdapter = loadAutoCompleteAdapter();
            if (userAdapter != null)
                autoCompleteUsers.setAdapter(userAdapter);

            cancelTravel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cleanDialog();
                }
            });
            addUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertUser();
                }
            });
            deleteUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteUser();
                }
            });
            addTravel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendNewTravel();
                }
            });
        }
    }

    private void sendNewTravel() {
        String travelNameString = travelName.getText().toString();
        String travelDesString = travelDescription.getText().toString();
        if (travelNameString.equals("") || travelDesString.equals("")) {
            Toast.makeText(travelListActivity.this, this.getString(R.string.new_travel_dialog_user_list_empty_name_description), Toast.LENGTH_SHORT).show();
            return;
        }
        if (UserContent.isEmpty()) {
            Toast.makeText(travelListActivity.this, this.getString(R.string.new_travel_dialog_user_list_empty_list), Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject request = new JSONObject();
        newTravel = new Travel();
        newTravel.setName(travelNameString);
        newTravel.setDescription(travelDesString);
        User admin = new User();
        admin.setEMail(user.getEMail());
        admin.setId(user.getId());
        admin.setName(user.getName());
        newTravel.setAdmin(admin);
        HashMap<String, User> newTravelPeople = new HashMap<String, User>();
        try {
            request.put("name", travelNameString).put("description", travelDesString);
            JSONArray arrayUsers = new JSONArray();
            UserContent.UserItem[] userItems = UserContent.getUsers();
            for (UserContent.UserItem userItem : userItems) {
                String id = userItem.id;
                JSONObject user = new JSONObject().put("id", id);
                User toAdd = users.get(id);
                newTravelPeople.put(String.valueOf(toAdd.getId()) , toAdd);
                arrayUsers.put(user);
            }
            request.put("users", arrayUsers);
            newTravel.setPeople(newTravelPeople);
            TravelListHandler addNewTravel = new TravelListHandler(this, user);
            String[] requestParameters = {"newTravel", request.toString()};
            addNewTravel.execute(requestParameters);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cleanDialog() {
        newTravelDialog.dismiss();
        UserContent.cleanList();
        travelName.setText("");
        travelDescription.setText("");
        autoCompleteUsers.setText("");
    }

    private void deleteUser() {
        UserContent.UserItem[] selected = UserContent.getSelected();
        if (selected == null) {
            Toast.makeText(travelListActivity.this, this.getString(R.string.new_travel_dialog_user_list_empty_selected), Toast.LENGTH_SHORT).show();
            return;
        }
        for (UserContent.UserItem aSelected : selected) {
            String id = aSelected.id;
            UserContent.deleteItem(id);
        }
        adapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> loadAutoCompleteAdapter() {
        if (users != null) {
            int length = users.size();
            final String[] usersItems = new String[length];
            Iterator<String> iterator = users.keySet().iterator();
            int index = 0;
            while (iterator.hasNext()) {
                String current = iterator.next();
                usersItems[index++] = users.get(current).getEMail();
            }
            return new ArrayAdapter<String>(this, simple_dropdown_item_1line, usersItems);
        }
        return null;
    }

    private User getUser(String userMail) {
        if (users != null) {
            for (String current : users.keySet()) {
                if (users.get(current).getEMail().equals(userMail)) {
                    return users.get(current);
                }
            }
        }
        return null;
    }

    private void insertUser() {
        String textToAdd = autoCompleteUsers.getText().toString();
        autoCompleteUsers.setText("");
        if (!EmailValidator.validate(textToAdd)) {
            Toast.makeText(travelListActivity.this, this.getString(R.string.new_travel_dialog_email_error), Toast.LENGTH_SHORT).show();
            return;
        }
        if (textToAdd.equals(user.getEMail())) {
            Toast.makeText(travelListActivity.this, this.getString(R.string.new_travel_dialog_user_error), Toast.LENGTH_SHORT).show();
            return;
        }
        User fromMap = getUser(textToAdd);
        if (fromMap == null) {
            Toast.makeText(travelListActivity.this, this.getString(R.string.new_travel_dialog_user_not_found), Toast.LENGTH_SHORT).show();
            return;
        }
        if (UserContent.isInTheList(String.valueOf(fromMap.getId()))) {
            Toast.makeText(travelListActivity.this, this.getString(R.string.new_travel_dialog_user_list_already_added), Toast.LENGTH_SHORT).show();
            return;
        }
        UserContent.addItem(new UserContent.UserItem(String.valueOf(fromMap.getId()), fromMap.getName(), fromMap.getEMail(), false));
        adapter.notifyDataSetChanged();
    }

    public void updateNewTrip(JSONObject response) {
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
                    updateTravelList(response);
            }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private void updateTravelList(JSONObject response) {
        try {
            newTravel.setId(response.getInt("id"));
            TravelContent.addItem(new TravelContent.TravelItem(String.valueOf(newTravel.getId()), newTravel.getName(), newTravel.getDescription()));
            travels.put(String.valueOf(newTravel.getId()), newTravel);
            travelListFragment.adapter.notifyDataSetChanged();
            cleanDialog();
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
}
