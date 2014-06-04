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
    public  CheckBox ferries;
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

    public void createDirectionsDialog(User me, Object destination){
        String departureAddress = me.getAddress();
        String destinationAddress = "";
        double lat1 = me.getLatitude();
        double lng1 = me.getLongitude();
        double lat2;
        double lng2;
        if(destination instanceof User){
           destinationAddress = ((User)destination).getAddress();
           lat2 = ((User)destination).getLatitude();
           lng2 = ((User)destination).getLongitude();
        }else{
            destinationAddress = ((MeetingPoint)destination).getAddress();
            lat2 = ((MeetingPoint)destination).getLatitude();
            lng2 = ((MeetingPoint)destination).getLongitude();
        }
        if(directionsDialog == null){
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
                    travelMode.setPromptId(0);
                }
            });
            tolls = (CheckBox)directionsDialog.findViewById(R.id.directionsDialogTollCheckBox);
            highways = (CheckBox)directionsDialog.findViewById(R.id.directionsDialogHighwaysCheckBox);
            ferries = (CheckBox)directionsDialog.findViewById(R.id.directionsDialogFerriesCheckBox);
            travelMode = (Spinner)directionsDialog.findViewById(R.id.directionsDialogModeSpinner);
            getDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //TODO get query parameters and send a request to google directions API servers through custom AyncTask Handler
                }
            });
        }
        from.setText(departureAddress);
        to.setText(destinationAddress);



    }


}
