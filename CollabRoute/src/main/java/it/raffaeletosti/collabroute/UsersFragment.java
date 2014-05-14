package it.raffaeletosti.collabroute;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Set;

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
            e.printStackTrace();
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


    private static void checkForUpdate(JSONArray array) {
        HashMap<String, User> newMap = new HashMap<String, User>();
        int length = array.length();
        String idSelected = UsersListContent.getSelected();
        UsersListContent.cleanList();
        try {
            for (int i = 0; i < length; i++) {
                JSONObject item = array.getJSONObject(i);
                //System.err.println(item.toString());
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
