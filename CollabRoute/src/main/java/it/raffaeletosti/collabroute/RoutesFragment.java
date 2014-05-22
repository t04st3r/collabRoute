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

import java.util.HashMap;

import it.raffaeletosti.collabroute.model.MeetingPoint;
import it.raffaeletosti.collabroute.model.User;
import it.raffaeletosti.collabroute.routes.CustomArrayAdapterRoutes;
import it.raffaeletosti.collabroute.routes.RoutesContent;

public class RoutesFragment extends Fragment {

    private static ListView routesListView;
    private static ArrayAdapter routesListAdapter;
    private static Activity thisActivity;
    private Button addRoute;
    private Button modifyRoute;
    private Button deleteRoute;
    private Button visualizeRouteOnMap;
    private Button getDirections;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RoutesFragment.
     */
    public static RoutesFragment newInstance() {
        RoutesFragment fragment = new RoutesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public RoutesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_routes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        thisActivity = getActivity();
        routesListView = (ListView)thisActivity.findViewById(R.id.routesListView);
        routesListAdapter = new CustomArrayAdapterRoutes(thisActivity, R.layout.route_row, RoutesContent.ITEMS);
        routesListView.setAdapter(routesListAdapter);
        routesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        addRoute = (Button)thisActivity.findViewById(R.id.add_route_button);
        modifyRoute = (Button)thisActivity.findViewById(R.id.modify_route_button);
        deleteRoute = (Button)thisActivity.findViewById(R.id.delete_route_button);
        visualizeRouteOnMap = (Button)thisActivity.findViewById(R.id.visualize_route_on_map_button);
        getDirections = (Button)thisActivity.findViewById(R.id.get_directions_to_route);
        //TODO onClick listener for every buttons with handler methods
        fillListFromModel();
        updateRoutesList();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RoutesContent.cleanList();

    }

    private static void updateRoutesList() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                routesListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void fillListFromModel(){
        HashMap<String, MeetingPoint> routesList = TravelActivity.travel.getRoutes();
        if(!routesList.isEmpty()){
            for(String current : routesList.keySet()){
                MeetingPoint currentMP = routesList.get(current);
                User creator = currentMP.getIdUser() == TravelActivity.travel.getAdmin().getId() ? TravelActivity.travel.getAdmin()
                        : TravelActivity.travel.getPeople().get(currentMP.getIdUser());
                RoutesContent.RoutesItem item = new RoutesContent.RoutesItem(String.valueOf(currentMP.getId()),
                        currentMP.getAddress(), creator.getName(), currentMP.getLatitude(), currentMP.getLongitude(), false);
                RoutesContent.addItem(item);
            }
        }
    }

}
