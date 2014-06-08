package it.raffaeletosti.collabroute;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.users.CustomArrayAdapterUsersList;
import it.raffaeletosti.collabroute.users.UsersListContent;
import it.raffaeletosti.collabroute.model.User;

/**
 * Created by raffaele on 05/05/14.
 */
public class UsersFragment extends Fragment {

    private static ListView chatUsersStatus;
    private static ArrayAdapter usersListAdapter;
    private static Activity thisActivity;
    private Button visualizeOnTheMap;
    private Button getDirections;
    private Button visualizeAllUsers;
    private static double MIN_DISTANCE = 200; //meters
    public static UsersFragment newInstance() {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        thisActivity = getActivity();
        chatUsersStatus = (ListView) thisActivity.findViewById(R.id.usersListView);
        usersListAdapter = new CustomArrayAdapterUsersList(thisActivity, R.layout.chat_user_row, UsersListContent.ITEMS);
        chatUsersStatus.setAdapter(usersListAdapter);
        chatUsersStatus.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        visualizeOnTheMap = (Button) thisActivity.findViewById(R.id.visualize_map_button);
        visualizeAllUsers = (Button) thisActivity.findViewById(R.id.visualize_all_button);
        getDirections = (Button) thisActivity.findViewById(R.id.users_routes_button);

        visualizeOnTheMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UsersListContent.nobodySelected()) {
                    showNobodySelectedMessage();
                    return;
                }
                showUserOnMap();
            }
        });

        getDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UsersListContent.nobodySelected()) {
                    showNobodySelectedMessage();
                    return;
                }
                getDirectionsToSelectedUser();

            }//TODO get directions using google Directions API and find a way to shows them on the map of course in a separate method as well!! :P
        });

        visualizeAllUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TravelActivity.map.updateCameraMapUsers();
                TravelActivity.mViewPager.setCurrentItem(0);
            }
        });
    }

    private void showUserOnMap() {
        String id = UsersListContent.getSelected();
        if (UsersListContent.isOnLine(id)) {
            User selected = !id.equals(String.valueOf(TravelActivity.travel.getAdmin().getId())) ?
                    TravelActivity.travel.getPeople().get(id) : TravelActivity.travel.getAdmin();
            TravelActivity.map.updateCameraSingleUser(selected);
            TravelActivity.mViewPager.setCurrentItem(0);
        } else {
            Toast.makeText(thisActivity, getString(R.string.user_offline_alert), Toast.LENGTH_SHORT).show();
        }
    }


    private void getDirectionsToSelectedUser() {
        String idSelected = UsersListContent.getSelected();
        String idMyself = String.valueOf(TravelActivity.user.getId());
        if (!UsersListContent.isOnLine(idSelected)) {
            Toast.makeText(thisActivity, getString(R.string.user_offline_alert), Toast.LENGTH_SHORT).show();
            return;
        }
        if(idSelected.equals(String.valueOf(TravelActivity.user.getId()))){
            Toast.makeText(thisActivity, getString(R.string.directions_to_yourself), Toast.LENGTH_SHORT).show();
            return;
        }
        User selected = !idSelected.equals(String.valueOf(TravelActivity.travel.getAdmin().getId())) ?
                TravelActivity.travel.getPeople().get(idSelected) : TravelActivity.travel.getAdmin();
        User mySelf = !idMyself.equals(String.valueOf(TravelActivity.travel.getAdmin().getId())) ?
                TravelActivity.travel.getPeople().get(idMyself) : TravelActivity.travel.getAdmin();
        double lat1 = selected.getLatitude();
        double lng1 = selected.getLongitude();
        double lat2 = mySelf.getLatitude();
        double lng2 = mySelf.getLongitude();
        if((TravelActivity.map.calculateDistance(lat1,lng1,lat2,lng2)) < MIN_DISTANCE ){
            Toast.makeText(thisActivity, getString(R.string.directions_error_too_near), Toast.LENGTH_SHORT).show();
            return;
        }
        TravelActivity.directions.createDirectionsDialog(mySelf, selected);
        TravelActivity.directions.directionsDialog.show();

    }

    protected static void fillUsersStatus(JSONArray usersListArray) {
        try {
            String result = usersListArray.getJSONObject(0).getString("result");
            if (!result.equals("OK")) {
                Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
            } else {
                JSONArray list = usersListArray.getJSONObject(0).getJSONArray("list");
                checkForUpdate(list);
            }
            updateUsersList();
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private static void updateUsersList() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                usersListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showNobodySelectedMessage() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(thisActivity, getString(R.string.no_user_selected), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private static void checkForUpdate(JSONArray array) {
        HashMap<String, User> newMap = new HashMap<String, User>();
        int length = array.length();
        String idSelected = UsersListContent.getSelected();
        UsersListContent.cleanList();
        try {
            for (int i = 0; i < length; i++) {
                JSONObject item = array.getJSONObject(i);
                int id = item.getInt("id");
                boolean selected = idSelected != null && Integer.parseInt(idSelected) == id ? true : false;
                String name = item.getString("name");
                String mail = item.getString("email");
                double latitude = item.getDouble("latitude");
                double longitude = item.getDouble("longitude");
                String address = item.getString("address");
                if (id != TravelActivity.travel.getAdmin().getId()) {
                    User newUser = new User(id, name, mail, latitude, longitude, address);
                    newMap.put(String.valueOf(id), newUser);
                    UsersListContent.addItem(new UsersListContent.UsersListItem(String.valueOf(id), name, formatString(latitude, longitude, address), item.getBoolean("onLine"), selected, false));
                } else {
                    User admin = TravelActivity.travel.getAdmin();
                    admin.setName(name);
                    admin.setEMail(mail);
                    admin.setLatitude(latitude);
                    admin.setLongitude(longitude);
                    admin.setAddress(address);
                    UsersListContent.addItem(new UsersListContent.UsersListItem(String.valueOf(id), name, formatString(latitude, longitude, address), item.getBoolean("onLine"), selected, true));
                }
            }
            TravelActivity.travel.setPeople(newMap);
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UsersListContent.cleanList();

    }

    private static String formatString(double latitude, double longitude, String address) {
        NumberFormat formatter = new DecimalFormat("#000.00000");
        return address + "\n(LAT: " + formatter.format(latitude) + " LNG: " + formatter.format(longitude) + ")";
    }
}
