package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;


import it.raffaeletosti.collabroute.connection.CoordinatesHandler;


public class GMapFragment extends Fragment implements android.location.LocationListener {

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Activity activity;
    protected Location currentLocation;
    protected LocationClient client;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    protected GoogleMap map;
    protected View view;
    private Handler mUserLocationHandler = null;

    protected GooglePlayServicesClient.ConnectionCallbacks mConnectionCallbacks =
            new GooglePlayServicesClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    currentLocation = client.getLastLocation();
                }

                @Override
                public void onDisconnected() {
                    Toast.makeText(activity, getString(R.string.google_play_error_message), Toast.LENGTH_SHORT).show();
                }
            };

    private GooglePlayServicesClient.OnConnectionFailedListener mConnectionFailedListener =
            new GooglePlayServicesClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {
                    if (connectionResult.hasResolution()) {
                        try {
                            connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                        } catch (IntentSender.SendIntentException e) {
                            System.err.println(e);
                        }
                    } else {
                        System.err.println("CAN'T CONNECT: " + connectionResult.getErrorCode());
                    }
                }
            };


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = getActivity();
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 0, this); //update location ever 20 seconds
        client = new LocationClient(activity, mConnectionCallbacks, mConnectionFailedListener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager manager = getFragmentManager();
        Fragment existingFragment = manager.findFragmentById(R.id.googleMap);
        if(existingFragment == null && currentLocation != null){
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(R.id.mapLayout, mapFragment);
            ft.commit();
            manager.executePendingTransactions();
            map = mapFragment.getMap();
        }
        else{
            map = ((SupportMapFragment)existingFragment).getMap();
        }
        if(map != null) {
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            //TODO embed map camera view with markers and users possibly in a separate method
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
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
        super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }

        } else
            try {
                view = inflater.inflate(R.layout.fragment_gmap, container, false);
            } catch (InflateException e) {
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
        handler.execute(String.valueOf(location.getLongitude()), String.valueOf(location.getLatitude()));
        System.err.println(" LONG: "+String.valueOf(location.getLongitude()+"LAT: "+String.valueOf(location.getLatitude())));
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
