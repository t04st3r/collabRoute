package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.CoordinatesHandler;
import it.raffaeletosti.collabroute.model.User;


public class GMapFragment extends Fragment implements android.location.LocationListener {

    protected LocationManager locationManager;
    protected Activity activity;
    protected Location currentLocation;
    protected LocationClient client;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    protected GoogleMap map;
    protected View view;
    public HashMap<String, Marker> markers;
    public Handler MarkerHandlerThread;
    public Runnable run;

    private final static int UPDATE_TIME_RANGE = 5000; //millisecond
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
        MarkerHandlerThread = new Handler(Looper.getMainLooper());
        run = new Runnable(){

            @Override
            public void run() {
                    if (markers == null) {
                        createMarkers();
                    } else {
                        updateMarkers();
                    }
            }

        };
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
    }

    void createMarkers() {
        HashMap<String, User> users = TravelActivity.travel.getPeople();
        markers = new HashMap<String, Marker>();
        User admin = TravelActivity.travel.getAdmin();
        setSingleMarker(admin);
        for (String current : users.keySet()) {
            User currentUser = users.get(current);
            setSingleMarker(currentUser);
        }
        updateCameraMapUsers();

    }

    void setSingleMarker(User user) {
        double lat = user.getLatitude();
        double lng = user.getLongitude();
        LatLng position = new LatLng(lat, lng);
        MarkerOptions options = new MarkerOptions()
                .position(position).title(user.getName())
                .snippet(user.getAddress())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                .draggable(false);
        boolean isOnLine = lat == 0 && lng == 0 ? false : true;
        options.visible(isOnLine);
        Marker currentMarker = map.addMarker(options);
        currentMarker.showInfoWindow();
        markers.put(String.valueOf(user.getId()), currentMarker);

    }

    void updateSingleMarker(User user) {
        if (markers != null) {
            Marker current = markers.get(String.valueOf(user.getId()));
            double lat = user.getLatitude();
            double lng = user.getLongitude();
            LatLng position = new LatLng(lat, lng);
            boolean isOnLine = lat == 0 && lng == 0 ? false : true;
            current.setVisible(isOnLine);
            current.setPosition(position);
            current.setTitle(user.getName());
            current.setSnippet(user.getAddress());
            current.showInfoWindow();
        }
    }

    void updateMarkers() {
        if (markers != null) {
            updateSingleMarker(TravelActivity.travel.getAdmin());
            for (String current : markers.keySet()) {
                User currentUser = TravelActivity.travel.getPeople().get(current);
                if (currentUser != null) {
                    updateSingleMarker(currentUser);
                }
            }
        }
        updateCameraMapUsers();
    }

    void updateCameraMapUsers() {
        User[] maxDistUsers = calculateMaxDistance();
        if (maxDistUsers[1] != null) {
            LatLng user1 = new LatLng(maxDistUsers[0].getLatitude(), maxDistUsers[0].getLongitude());
            LatLng user2 = new LatLng(maxDistUsers[1].getLatitude(), maxDistUsers[1].getLongitude());
            LatLngBounds latLngBounds = new LatLngBounds(user1, user2);
            CameraUpdate updateCameraViewWithBounds = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);
            map.moveCamera(updateCameraViewWithBounds);
        } else {
            updateCameraSingleUser(maxDistUsers[0]);
        }
    }

    void updateCameraSingleUser(User user) {
        LatLng userLatLng = new LatLng(user.getLatitude(), user.getLongitude());
        CameraUpdate updateCameraView = CameraUpdateFactory.newLatLng(userLatLng);
        map.moveCamera(updateCameraView);
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

        //if distance are bigger that maxDistance send it to the server
        if (calculateDistance(lat1, long1, lat2, long2) > MAX_DISTANCE) {
            CoordinatesHandler handler = new CoordinatesHandler(activity, TravelActivity.user, this, String.valueOf(TravelActivity.travel.getId()));
            handler.execute(String.valueOf(long2), String.valueOf(lat2));
            System.err.println("Coordinate update request because of distance change");
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

    private double calculateDistance(double lat1, double long1, double lat2, double long2) {
        final double EARTH_RADIUS = 6372795.477598; //meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLong = Math.toRadians(long2 - long1);
        double sinDLat = Math.sin(dLat / 2);
        double sinDLong = Math.sin(dLong / 2);
        double a = Math.pow(sinDLat, 2) + Math.pow(sinDLong, 2) * Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2));
        double d = Math.asin(Math.sqrt(a)) * 2 * EARTH_RADIUS;
        //System.err.println("Distance: "+d);
        return d;
    }

    User[] calculateMaxDistance() {
        double maxDistance = 0;
        User user1 = TravelActivity.travel.getAdmin();
        User user2 = null;
        HashMap<String, User> userHashMap = TravelActivity.travel.getPeople();
        for (String current : userHashMap.keySet()) {
            double lat1 = user1.getLatitude();
            double lng1 = user1.getLongitude();
            double lat2 = userHashMap.get(current).getLatitude();
            double lng2 = userHashMap.get(current).getLongitude();
            if (lat1 != 0.0d && lng1 != 0.0d && lat2 != 0.0d && lng2 != 0.0d) {
                double distance = calculateDistance(lat1, lng1, lat2, lng2);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    user2 = userHashMap.get(current);
                }
            }
        }
        int size = userHashMap.size();
        int count = 1;
        HashMap<String, User> secondMap = TravelActivity.travel.cloneUsersMap();
        for (String current1 : userHashMap.keySet()) {
            for (String current2 : secondMap.keySet()) {
                if (current1 != current2) {
                    double lat1 = userHashMap.get(current1).getLatitude();
                    double lng1 = userHashMap.get(current1).getLongitude();
                    double lat2 = secondMap.get(current2).getLatitude();
                    double lng2 = secondMap.get(current2).getLongitude();
                    if (lat1 != 0.0d && lng1 != 0.0d && lat2 != 0.0d && lng2 != 0.0d) {
                        double distance = calculateDistance(lat1, lng1, lat2, lng2);
                        if (distance > maxDistance) {
                            maxDistance = distance;
                            user1 = userHashMap.get(current1);
                            user2 = secondMap.get(current2);
                        }
                    }
                }
                if (count == size) {
                    secondMap.remove(current1);
                    size--;
                    count = 1;
                } else
                    count++;
            }
        }
        User[] toReturn = {user1, user2};
        return toReturn;

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
                    Toast.makeText(activity, ConnectionHandler.errors.get(ConnectionHandler.WRONG_COORDINATES), Toast.LENGTH_SHORT).show();
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