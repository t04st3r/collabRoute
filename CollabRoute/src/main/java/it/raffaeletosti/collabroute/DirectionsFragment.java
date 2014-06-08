package it.raffaeletosti.collabroute;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.DirectionsHandler;
import it.raffaeletosti.collabroute.directions.CustomArrayAdapterDirections;
import it.raffaeletosti.collabroute.directions.DirectionsContent;
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
    public TextView fromFrag;
    public TextView toFrag;
    public TextView distanceFrag;
    public TextView durationFrag;
    public TextView travelModeFrag;
    public TextView avoidFrag;
    String avoidString;
    String travelModeString;
    LatLng originLatLng;
    LatLng destinationLatLng;

    private static ListView directionListView;
    private static ArrayAdapter directionsArrayAdapter;

    private enum ResponseMSG {OK, ZERO_RESULTS, OVER_QUERY_LIMIT, REQUEST_DENIED, NOT_FOUND, UNKNOWN_ERROR, INVALID_REQUEST, CONN_TIMEDOUT, CONN_REFUSED, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR}


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
        fromFrag = (TextView) thisActivity.findViewById(R.id.fromTextView);
        toFrag = (TextView) thisActivity.findViewById(R.id.toTextView);
        distanceFrag = (TextView) thisActivity.findViewById(R.id.distanceTextView);
        durationFrag = (TextView) thisActivity.findViewById(R.id.durationTextView);
        travelModeFrag = (TextView) thisActivity.findViewById(R.id.modeTextView);
        avoidFrag = (TextView) thisActivity.findViewById(R.id.avoidTextView);
        directionListView = (ListView) thisActivity.findViewById(R.id.directionsListView);
        directionsArrayAdapter = new CustomArrayAdapterDirections(thisActivity, R.layout.directions_row, DirectionsContent.ITEMS);
        directionListView.setAdapter(directionsArrayAdapter);
    }

    public void createDirectionsDialog(User me, Object destination) {
        avoidString = "";
        travelModeString = "";
        String departureAddress = me.getAddress();
        String destinationAddress = "";
        double lat1 = me.getLatitude();
        double lng1 = me.getLongitude();
        originLatLng = new LatLng(lat1, lng1);
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
        destinationLatLng = new LatLng(lat2, lng2);
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
                    cleanDirectionsDialog();
                }
            });
            tolls = (CheckBox) directionsDialog.findViewById(R.id.directionsDialogTollCheckBox);
            highways = (CheckBox) directionsDialog.findViewById(R.id.directionsDialogHighwaysCheckBox);
            ferries = (CheckBox) directionsDialog.findViewById(R.id.directionsDialogFerriesCheckBox);
            travelMode = (Spinner) directionsDialog.findViewById(R.id.directionsDialogModeSpinner);
            getDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean[] checkBox = {tolls.isChecked(), highways.isChecked(), ferries.isChecked()};
                    buildQueryStringStartThread(checkBox, travelMode.getSelectedItem().toString(), originLatLng, destinationLatLng);
                    cleanDirectionsDialog();
                }
            });
        }
        from.setText(departureAddress);
        to.setText(destinationAddress);
    }

    private void cleanDirectionsDialog() {
        from.setText("");
        to.setText("");
        tolls.setChecked(false);
        highways.setChecked(false);
        ferries.setChecked(false);
        travelMode.setSelection(0);
    }

    private void buildQueryStringStartThread(boolean[] checkBox, String travelMode, LatLng origin, LatLng destination) {
        String queryString = "origin=" + String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude)
                + "&destination=" + String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude);
        int count = 0;
        if (checkBox[0] || checkBox[1] || checkBox[2]) {
            queryString += "&avoid=" + buildAvoidQueryString(checkBox);
        }
        queryString += "&mode=" + travelMode;
        travelModeString = travelMode;
        DirectionsHandler handler = new DirectionsHandler(thisActivity, queryString, this);
        handler.execute();
    }

    private String buildAvoidQueryString(boolean[] checkBox) {
        if (checkBox[0] && !checkBox[1] && !checkBox[2]) {
            avoidString = "tolls";
            return avoidString;
        }
        if (!checkBox[0] && checkBox[1] && !checkBox[2]) {
            avoidString = "highways";
            return avoidString;
        }
        if (!checkBox[0] && !checkBox[1] && checkBox[2]) {
            avoidString = "ferries";
            return avoidString;
        }
        if (checkBox[0] && checkBox[1] && !checkBox[2]) {
            avoidString = "tolls|highways";
            return avoidString;
        }
        if (checkBox[0] && !checkBox[1] && checkBox[2]) {
            avoidString = "tolls|ferries";
            return avoidString;
        }
        if (!checkBox[0] && checkBox[1] && checkBox[2]) {
            avoidString = "highways|ferries";
            return avoidString;
        }
        if (checkBox[0] && checkBox[1] && checkBox[2]) {
            avoidString = "tolls|highways|ferries";
            return avoidString;
        }
        return null;
    }

    public void fillListView(JSONObject response) throws JSONException {
        if (directionsDialog != null && directionsDialog.isShowing())
            directionsDialog.dismiss();
        String resultString = response.getString("status");
        ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
        switch (responseEnum) {
            case CONN_REFUSED:
                Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                return;
            case CONN_BAD_URL:
                Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                return;
            case CONN_GENERIC_IO_ERROR:
                Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                return;
            case CONN_GENERIC_ERROR:
                Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                return;
            case CONN_TIMEDOUT:
                Toast.makeText(thisActivity, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                return;
            case OK:
                fillDirections(response.getJSONArray("routes"));
                return;
            default:
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisActivity);
                alertDialogBuilder.setTitle(getString(R.string.geocode_dialog_title));
                alertDialogBuilder.setNegativeButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });
                alertDialogBuilder.setMessage(getString(R.string.directions_dialog_negative));
                alertDialogBuilder.show();
        }
    }

    private void fillDirections(JSONArray routes) {
        try {
            JSONObject legsObject = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0);
            fromFrag.setText(legsObject.getString("start_address"));
            toFrag.setText(legsObject.getString("end_address"));
            distanceFrag.setText(legsObject.getJSONObject("distance").getString("text"));
            durationFrag.setText(legsObject.getJSONObject("duration").getString("text"));
            travelModeFrag.setText(travelModeString);
            String avoidValue = avoidString.equals("") ? "nothing" : avoidString.replaceAll("\\|", " ");
            avoidFrag.setText(avoidValue);
            if (!DirectionsContent.isEmpty()) {
                DirectionsContent.cleanList();
            }
            JSONArray steps = legsObject.getJSONArray("steps");
            int length = steps.length();
            for (int i = 0; i < length; i++) {
                JSONObject step = steps.getJSONObject(i);
                String stepDuration = step.getJSONObject("duration").getString("text");
                String stepDistance = step.getJSONObject("distance").getString("text");
                JSONObject startLocation = step.getJSONObject("start_location");
                JSONObject endLocation = step.getJSONObject("end_location");
                LatLng startLatLng = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));
                LatLng endLatLng = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));
                DirectionsContent.DirectionsItem item = new DirectionsContent.DirectionsItem(
                        String.valueOf(CustomArrayAdapterDirections.counter++), step.getString("html_instructions"),
                        stepDuration, endLatLng, startLatLng, step.getString("travel_mode").toLowerCase(),
                        stepDistance);
                DirectionsContent.addItem(item);
            }
            CustomArrayAdapterDirections.counter = 1;
            updateDirectionsList();
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

    private void updateDirectionsList() {
        thisActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                directionsArrayAdapter.notifyDataSetChanged();
                TravelActivity.mViewPager.setCurrentItem(2);
            }
        });
    }
}