package it.raffaeletosti.collabroute;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by raffaele on 16/05/14.
 */
public class DirectionsFragment extends Fragment {

    public Activity thisActivity;

    public static DirectionsFragment newInstance() {
        DirectionsFragment fragment = new DirectionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public DirectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_directions, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        thisActivity = getActivity();
    }
}
