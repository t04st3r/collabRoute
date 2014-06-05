package it.raffaeletosti.collabroute;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import it.raffaeletosti.collabroute.connection.DirectionsHandler;
import it.raffaeletosti.collabroute.model.MeetingPoint;
import it.raffaeletosti.collabroute.model.User;

/**
 * Created by raffaele on 16/05/14.
 */
public class DirectionsFragment extends Fragment {

    public Activity thisActivity;
    public Dialog directionsDialog;
    public Button getDirections;
    public Button cancelButton;
    public CheckBox tolls;
    public CheckBox highways;
    public CheckBox ferries;
    public Spinner travelMode;
    public TextView from;
    public TextView to;
    private static ListView directionListView;
    private static ArrayAdapter directionsArrayAdapter;

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

    public void createDirectionsDialog(User me, Object destination) {
        String departureAddress = me.getAddress();
        String destinationAddress = "";
        double lat1 = me.getLatitude();
        double lng1 = me.getLongitude();
        final LatLng origin = new LatLng(lat1, lng1);
        double lat2;
        double lng2;
        if (destination instanceof User) {
            destinationAddress = ((User) destination).getAddress();
            lat2 = ((User) destination).getLatitude();
            lng2 = ((User) destination).getLongitude();
        } else {
            destinationAddress = ((MeetingPoint) destination).getAddress();
            lat2 = ((MeetingPoint) destination).getLatitude();
            lng2 = ((MeetingPoint) destination).getLongitude();
        }
        final LatLng dest = new LatLng(lat2, lng2);
        if (directionsDialog == null) {
            directionsDialog = new Dialog(thisActivity);
            directionsDialog.setContentView(R.layout.directions_dialog);
            directionsDialog.setTitle(R.string.directions_dialog_get_directions);
            directionsDialog.setCancelable(false);
            directionsDialog.setCanceledOnTouchOutside(false);
            from = (TextView) directionsDialog.findViewById(R.id.directionsDialogFrom);
            to = (TextView) directionsDialog.findViewById(R.id.directionsDialogTo);
            getDirections = (Button) directionsDialog.findViewById(R.id.getDirectionsButton);
            cancelButton = (Button) directionsDialog.findViewById(R.id.cancelDialogButton);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    directionsDialog.dismiss();
                    from.setText("");
                    to.setText("");
                    tolls.setChecked(false);
                    highways.setChecked(false);
                    ferries.setChecked(false);
                    travelMode.setSelection(0);
                }
            });
            tolls = (CheckBox) directionsDialog.findViewById(R.id.directionsDialogTollCheckBox);
            highways = (CheckBox) directionsDialog.findViewById(R.id.directionsDialogHighwaysCheckBox);
            ferries = (CheckBox) directionsDialog.findViewById(R.id.directionsDialogFerriesCheckBox);
            travelMode = (Spinner) directionsDialog.findViewById(R.id.directionsDialogModeSpinner);
            getDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean[] checkBox = {tolls.isChecked() , highways.isChecked() , ferries.isChecked()};
                    buildQueryStringStartThread(checkBox, travelMode.getSelectedItem().toString(), origin, dest);
                }
            });
        }
        from.setText(departureAddress);
        to.setText(destinationAddress);
    }

    private void buildQueryStringStartThread(boolean[] checkBox, String travelMode, LatLng origin, LatLng destination) {
        String queryString = "origin=" + String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude)
                + "&destination=" + String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude);
        int count = 0;
        if (checkBox[0] || checkBox[1] || checkBox[2]) {
            queryString += "&avoid=" + buildAvoidQueryString(checkBox);
        }
        queryString += "&mode=" + travelMode;
        DirectionsHandler handler = new DirectionsHandler(thisActivity, queryString, this);
        handler.execute();
    }

    private String buildAvoidQueryString(boolean[] checkBox) {
        if (checkBox[0] && !checkBox[1] && !checkBox[2])
            return "tolls";
        if (!checkBox[0] && checkBox[1] && !checkBox[2])
            return "highways";
        if (!checkBox[0] && !checkBox[1] && checkBox[2])
            return "ferries";
        if (checkBox[0] && checkBox[1] && !checkBox[2])
            return "tolls|highways";
        if (checkBox[0] && !checkBox[1] && checkBox[2])
            return "tolls|ferries";
        if (!checkBox[0] && checkBox[1] && checkBox[2])
            return "highways|ferries";
        if (checkBox[0] && checkBox[1] && checkBox[2])
            return "tolls|highways|ferries";
        return null;
    }

    public void fillListView(JSONObject response) throws JSONException {
        System.err.println(response.toString());
    }
}
