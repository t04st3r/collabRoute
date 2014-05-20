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


public class RoutesFragment extends Fragment {

    private static ListView routesListView;
    private static ArrayAdapter routesListAdapter;
    private static Activity thisActivity;
    private Button addRoute;
    private Button modifyRoute;
    private Button DeleteRoute;
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

    }
}
