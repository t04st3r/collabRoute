package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;



import it.raffaeletosti.collabroute.connection.CoordinatesHandler;



public class GMapFragment extends Fragment implements android.location.LocationListener{

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Activity activity;
    protected Location currentLocation;
    protected LocationClient client;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap map;
    private static View view;

    private GooglePlayServicesClient.ConnectionCallbacks mConnectionCallbacks =
            new GooglePlayServicesClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            currentLocation = client.getLastLocation();
            Toast.makeText(activity, "CURRENT LOCATION: "+currentLocation, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected() {

        }
    };

    private GooglePlayServicesClient.OnConnectionFailedListener mConnectionFailedListener =
            new GooglePlayServicesClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    if(connectionResult.hasResolution()){
                        try {
                            connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                        } catch (IntentSender.SendIntentException e) {
                            System.err.println(e);
                        }
                    }else{
                        System.err.println("CAN'T CONNECT: "+connectionResult.getErrorCode());
                    }
                }
            };




    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        activity = getActivity();
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, this); //update location ever 20 seconds
        client = new LocationClient(activity, mConnectionCallbacks, mConnectionFailedListener);
    }

    @Override
    public void onStart(){
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop(){
        client.disconnect();
        super.onStop();
    }

    public static GMapFragment newInstance() {
        GMapFragment fragment = new GMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(view != null){
            ViewGroup parent = (ViewGroup) view.getParent();
            if(parent != null){
                parent.removeView(view);
            }
        }
        try{
            view = inflater.inflate(R.layout.fragment_gmap, container, false);
        }catch(InflateException e){
            System.err.println(e);
        }
        return view;
    }

    public GMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onLocationChanged(Location location) { //useful for updating location on client location changes
        CoordinatesHandler handler = new CoordinatesHandler(activity, TravelActivity.user);
        handler.execute(String.valueOf(location.getLongitude()) ,String.valueOf(location.getLatitude()));
        System.err.println("LATITUDE: "+location.getLatitude()+"  LONGITUDE: "+location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
