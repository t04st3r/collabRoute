package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;


import org.json.JSONException;
import org.json.JSONObject;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.CoordinatesHandler;
import it.raffaeletosti.collabroute.connection.UserLoginHandler;


public class GMapFragment extends Fragment implements android.location.LocationListener {

    protected LocationManager locationManager;
    protected Activity activity;
    protected Location currentLocation;
    protected LocationClient client;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    protected GoogleMap map;
    protected View view;

    private final static int UPDATE_TIME_RANGE = 10000; //millisecond
    private final static double MAX_DISTANCE = 10; //meters

    private enum ResponseMSG {OK, DATABASE_ERROR, CONN_TIMEDOUT, WRONG_COORDINATES, CONN_REFUSED, GEOCODE_API_ERROR, CONN_BAD_URL, CONN_GENERIC_IO_ERROR, CONN_GENERIC_ERROR}

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIME_RANGE, 0, this);
        client = new LocationClient(activity, mConnectionCallbacks, mConnectionFailedListener);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentManager manager = getFragmentManager();
        Fragment existingFragment = manager.findFragmentById(R.id.googleMap);
        if (existingFragment == null && currentLocation != null) {
            SupportMapFragment mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(R.id.mapLayout, mapFragment);
            ft.commit();
            manager.executePendingTransactions();
            map = mapFragment.getMap();
        } else {
            map = ((SupportMapFragment) existingFragment).getMap();
        }
        if (map != null) {
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
        double lat2 = location.getLatitude();
        double long2 = location.getLongitude();
        double lat1 = TravelActivity.user.getLatitude();
        double long1 = TravelActivity.user.getLongitude();

        //if values are not initialized update send it to the server
        if (lat1 == 0.0d && long1 == 0.0d) {
            CoordinatesHandler handler = new CoordinatesHandler(activity, TravelActivity.user, this);
            handler.execute(String.valueOf(long2), String.valueOf(lat2));
            return;
        }

        //if distance are bigger that maxDistance send it to the server
        if (calculateDistance(lat1, long1, lat2, long2)) {
            CoordinatesHandler handler = new CoordinatesHandler(activity, TravelActivity.user, this);
            handler.execute(String.valueOf(long2), String.valueOf(lat2));
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.err.println("GPS UPDATE SERVICE STOPPED");
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }

    /* this function return true if distance between previous coordinates values
 * (lat1, long1) and the current values (lat2, long2) is bigger than MAX_DISTANCE (in meter) according to the
 *
 * Haversine formula: (d = distance, R = earth radius)
 *
 * haversine(d / R) = haversine(lat2 - lat1) + cos(lat1) * cos(lat2) * haversine(long2 - long1)
 *
 * where:
 *
 * haversine(x) = (sin(x / 2))^2
 * or
 * haversine(x) = (1 - cos(x)) / 2
 *
 * (http://en.wikipedia.org/wiki/Haversine_formula)
 * */

     private boolean calculateDistance(double lat1, double long1, double lat2, double long2) {
        final double EARTH_RADIUS = 6372795.477598; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLong = Math.toRadians(long2 - long1);
        double sinDLat = Math.sin(dLat / 2);
        double sinDLong = Math.sin(dLong / 2);
        double a = Math.pow(sinDLat, 2) + Math.pow(sinDLong, 2) * Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2));
        double d = Math.asin(Math.sqrt(a)) * 2 * EARTH_RADIUS;
        //System.err.println("Distance: "+d);
        return d > MAX_DISTANCE;
    }

    public void confirmationResponse(JSONObject response) {
        try {
            String resultString = response.getString("result");
            ResponseMSG responseEnum = ResponseMSG.valueOf(resultString);
            switch (responseEnum) {
                case CONN_REFUSED:
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.CONN_REFUSED), Toast.LENGTH_SHORT).show();
                    System.err.println("CONNECTION REFUSED");
                    return;
                case CONN_BAD_URL:
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.CONN_BAD_URL), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_IO_ERROR:
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_IO_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_GENERIC_ERROR:
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.CONN_GENERIC_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case CONN_TIMEDOUT:
                    System.err.println("CONNECTION REFUSED");
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.CONN_TIMEDOUT), Toast.LENGTH_SHORT).show();
                    return;
                case DATABASE_ERROR:
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.DB_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case WRONG_COORDINATES:
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.WRONG_COORDINATES),Toast.LENGTH_SHORT).show();
                    return;
                case GEOCODE_API_ERROR:
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.GEOCODE_API_ERROR), Toast.LENGTH_SHORT).show();
                    return;
                case OK:
                    double latitude = response.getDouble("latitude");
                    double longitude = response.getDouble("longitude");
                    String address = response.getString("address");
                    TravelActivity.user.setLatitude(latitude);
                    TravelActivity.user.setLongitude(longitude);
                    TravelActivity.user.setAddress(address);
                    //System.err.println("update coordinates LONG: " + String.valueOf(longitude) + " LAT: " + String.valueOf(latitude) + " ADDR: "+address);
                }
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
}