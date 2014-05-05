package it.raffaeletosti.collabroute;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;

import it.raffaeletosti.collabroute.chat.CustomArrayAdapterUsersList;
import it.raffaeletosti.collabroute.chat.UsersListContent;
import it.raffaeletosti.collabroute.model.User;

/**
 * Created by raffaele on 05/05/14.
 */
public class UsersFragment extends Fragment {

    private ListView chatUsersStatus;
    private ArrayAdapter usersListAdapter;
    private Activity thisActivity;

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
        if (chatUsersStatus == null) {
            chatUsersStatus = (ListView) thisActivity.findViewById(R.id.usersListView);
            fillUsersStatus();
            usersListAdapter = new CustomArrayAdapterUsersList(thisActivity, R.layout.chat_user_row, UsersListContent.ITEMS);
            chatUsersStatus.setAdapter(usersListAdapter);
            updateUsersList();
        }
    }

    private void fillUsersStatus() {
        UsersListContent.addItem(new UsersListContent.UsersListItem(String.valueOf(TravelActivity.travel.getAdmin().getId()),
                TravelActivity.travel.getAdmin().getName(),false));
        HashMap<String, User> users = TravelActivity.travel.getPeople();
        for (String current : users.keySet()) {
            UsersListContent.UsersListItem item = new UsersListContent.UsersListItem(current,
                    users.get(current).getName(), false);
            UsersListContent.addItem(item);
        }
    }

    private void updateUsersList(){
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                usersListAdapter.notifyDataSetChanged();
            }
        });
    }
}
