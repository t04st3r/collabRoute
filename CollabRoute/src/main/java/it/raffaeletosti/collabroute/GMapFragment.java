package it.raffaeletosti.collabroute;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Color;
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
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;


import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.raffaeletosti.collabroute.connection.ConnectionHandler;
import it.raffaeletosti.collabroute.connection.CoordinatesHandler;
import it.raffaeletosti.collabroute.connection.RoutesHandler;
import it.raffaeletosti.collabroute.directions.DirectionsContent;
import it.raffaeletosti.collabroute.model.MeetingPoint;
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
    public HashMap<String, Marker> routeMarkers;
    public HashMap<String, Marker> directionsMarker;
    public Handler MarkerHandlerThread;
    public Runnable run;
    private GoogleMap.InfoWindowAdapter windowAdapter;
    public Polyline directionsPolyLine;
    public boolean isMapClickable = false;


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
        routeMarkers = new HashMap<String, Marker>();
        directionsMarker = new HashMap<String, Marker>();
        MarkerHandlerThread = new Handler(Looper.getMainLooper());
        run = new Runnable() {

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
        if (map != null) {
            //set a Layout for the markers info window inserting title and snippets
            windowAdapter = new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View markerWindow = getLayoutInflater(null).inflate(R.layout.map_marker_view, null);
                    TextView name = (TextView) markerWindow.findViewById(R.id.user_name_text_view);
                    name.setText(marker.getTitle());
                    TextView address = (TextView) markerWindow.findViewById(R.id.address_text_view);
                    String snippetText = marker.getSnippet();
                    ImageView logo = (ImageView) markerWindow.findViewById(R.id.markerLogo);
                    WebView snippetWebView = (WebView) markerWindow.findViewById(R.id.snippetWebView);
                    if (snippetText != null && snippetText.contains("Created by:"))
                        logo.setImageResource(android.R.drawable.ic_menu_mylocation);
                    else
                        logo.setImageResource(android.R.drawable.ic_menu_myplaces);
                    address.setText(snippetText);
                    return markerWindow;
                }
            };
            map.setInfoWindowAdapter(windowAdapter);
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            map.setMyLocationEnabled(true);
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if(isMapClickable){
                        RoutesHandler reverseGeocodeHandler = new RoutesHandler(getActivity(), TravelActivity.route);
                        String queryString = "latlng="+String.valueOf(latLng.latitude)+","+String.valueOf(latLng.longitude);
                        TravelActivity.mViewPager.setCurrentItem(1);
                        reverseGeocodeHandler.execute("geocoding" , queryString);
                        isMapClickable = false;
                    }
                }
            });
        }
    }

    void createMarkers() {
        HashMap<String, User> users = TravelActivity.travel.getPeople();
        int mySelfId = TravelActivity.user.getId();
        User mySelf;
        markers = new HashMap<String, Marker>();
        User admin = TravelActivity.travel.getAdmin();
        mySelf = mySelfId == admin.getId() ? admin : users.get(String.valueOf(mySelfId));
        setSingleMarker(admin);
        for (String current : users.keySet()) {
            User currentUser = users.get(current);
            setSingleMarker(currentUser);
        }
        HashMap<String, MeetingPoint> routes = TravelActivity.travel.getRoutes();
        for (String current : routes.keySet()) {
            MeetingPoint currentMP = routes.get(current);
            setSingleMarker(currentMP);
        }
        updateCameraSingleUser(mySelf);

    }

    void setSingleMarker(User user) {
        double lat = user.getLatitude();
        double lng = user.getLongitude();
        float icon = user.getId() == TravelActivity.travel.getAdmin().getId() ? BitmapDescriptorFactory.HUE_BLUE : BitmapDescriptorFactory.HUE_VIOLET;
        LatLng position = new LatLng(lat, lng);
        MarkerOptions options = new MarkerOptions()
                .position(position).title(user.getName())
                .snippet(user.getAddress())
                .icon(BitmapDescriptorFactory.defaultMarker(icon))
                .alpha(0.9f)
                .draggable(false);
        boolean isOnLine = lat == 0 && lng == 0 ? false : true;
        options.visible(isOnLine);
        Marker currentMarker = map.addMarker(options);
        markers.put(String.valueOf(user.getId()), currentMarker);

    }

    void setSingleMarker(MeetingPoint mp) {
        double lat = mp.getLatitude();
        double lng = mp.getLongitude();
        MarkerOptions option = new MarkerOptions();
        option.position(new LatLng(lat, lng));
        option.title(mp.getAddress());
        String creatorId = String.valueOf(mp.getIdUser());
        String name = isAdmin(creatorId) ?
                TravelActivity.travel.getAdmin().getName() :
                TravelActivity.travel.getPeople().get(creatorId).getName();
        option.snippet("Created by: " + name);
        option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        option.visible(true);
        Marker currentMarker = map.addMarker(option);
        routeMarkers.put(String.valueOf(mp.getId()), currentMarker);
    }

    void updateSingleMarker(User user) {
        if (markers != null) {
            Marker current = markers.get(String.valueOf(user.getId()));
            double lat = user.getLatitude();
            double lng = user.getLongitude();
            LatLng position = new LatLng(lat, lng);
            boolean isOnLine = lat == 0.0f && lng == 0.0f ? false : true;
            current.setVisible(isOnLine);
            current.setPosition(position);
            current.setTitle(user.getName());
            current.setSnippet(user.getAddress());
            if (current.isInfoWindowShown()) {
                //only way since now to update snippet data on user position update it's pretty naive I know :(
                current.hideInfoWindow();
                current.showInfoWindow();
            }
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
            for (String current : routeMarkers.keySet()) {
                Marker currentMarker = routeMarkers.get(current);
                currentMarker.remove();
            }
            routeMarkers.clear();
            for (String current : TravelActivity.travel.getRoutes().keySet()) {
                setSingleMarker(TravelActivity.travel.getRoutes().get(current));
            }
        }
    }

    void updateCameraRoutes() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng singleRouteLatLng = null;
        if (routeMarkers != null && !routeMarkers.isEmpty()) {
            boolean isSingleRoute = routeMarkers.size() == 1 ? true : false;
            for (String current : routeMarkers.keySet()) {
                if (isSingleRoute) {
                    Marker marker = routeMarkers.get(current);
                    marker.showInfoWindow();
                    singleRouteLatLng = marker.getPosition();
                } else {
                    routeMarkers.get(current).hideInfoWindow();
                    builder.include(routeMarkers.get(current).getPosition());
                }
            }
            if (!isSingleRoute) {
                LatLngBounds bounds = builder.build();
                int padding = 200;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.animateCamera(cu);
                return;
            }
            CameraUpdate updateCameraView = CameraUpdateFactory.newLatLngZoom(singleRouteLatLng, 15);
            map.animateCamera(updateCameraView);
        }
    }

    void updateCameraMapUsers() {
        if (markers != null) {
            String idMarker = onlyOneMarkerVisible(markers);
            if (idMarker != null && routeMarkers.isEmpty()) {
                if (isAdmin(idMarker))
                    updateCameraSingleUser(TravelActivity.travel.getAdmin());
                else {
                    updateCameraSingleUser(TravelActivity.travel.getPeople().get(idMarker));
                }
                return;
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (String current : markers.keySet()) {
                markers.get(current).hideInfoWindow();
                if (markers.get(current).isVisible())
                    builder.include(markers.get(current).getPosition());
            }
            if (!routeMarkers.isEmpty()) {
                for (String current : routeMarkers.keySet()) {
                    routeMarkers.get(current).hideInfoWindow();
                    builder.include(routeMarkers.get(current).getPosition());
                }
            }
            LatLngBounds bounds = builder.build();
            int padding = 200;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(cu);
        }
    }

    private void setDirectionsMarkersInvisible(){
        if(directionsMarker != null && !directionsMarker.isEmpty()){
            for(String current: directionsMarker.keySet()){
                Marker currentMarker = directionsMarker.get(current);
                currentMarker.setVisible(false);
            }
        }
    }


    public void updateCameraTwoBounds(LatLng bounds1, LatLng bounds2, boolean isWholeJourney) {
        if(isWholeJourney){
            setDirectionsMarkersInvisible();
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder()
                .include(bounds1).include(bounds2);
        LatLngBounds bounds = builder.build();
        int padding = 200;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu);
    }

    private String onlyOneMarkerVisible(HashMap<String, Marker> markerHashMap) {
        int count = 0;
        String id = null;
        for (String current : markerHashMap.keySet()) {
            Marker currentMarker = markerHashMap.get(current);
            if (currentMarker.isVisible()) {
                count++;
                id = current;
            }
        }
        if (count == 1) {
            return id;
        }
        return null;
    }

    boolean isAdmin(String id) {
        return id.equals(String.valueOf(TravelActivity.travel.getAdmin().getId()));
    }

    void updateCameraSingleUser(User user) {
        LatLng userLatLng = new LatLng(user.getLatitude(), user.getLongitude());
        Marker currentMarker = markers.get(String.valueOf(user.getId()));
        currentMarker.showInfoWindow();
        CameraUpdate updateCameraView = CameraUpdateFactory.newLatLngZoom(userLatLng, 15);
        map.animateCamera(updateCameraView);
    }

    void updateCameraSingleRoute(MeetingPoint mp) {
        LatLng routeLatLng = new LatLng(mp.getLatitude(), mp.getLongitude());
        Marker currentMarker = routeMarkers.get(String.valueOf(mp.getId()));
        currentMarker.showInfoWindow();
        CameraUpdate updateCameraView = CameraUpdateFactory.newLatLngZoom(routeLatLng, 15);
        map.animateCamera(updateCameraView);
    }

    void setDirectionsMarkers(LatLng bound1, LatLng bound2, String wayPointIdString, String htmlSnip1, String htmlSnip2) {
        if (!directionsMarker.isEmpty()) {
            for (String current : directionsMarker.keySet()) {
                Marker currentMarker = directionsMarker.get(current);
                currentMarker.remove();
            }
            directionsMarker.clear();
        }
        int lastWayPointId = DirectionsContent.ITEMS.size();
        int wayPointId = Integer.parseInt(wayPointIdString);
        if (wayPointId != 1) {
            MarkerOptions option = new MarkerOptions();
            option.position(bound1);
            option.title("Waypoint " + wayPointId);
            option.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            option.visible(true);
            option.snippet(Jsoup.parse(htmlSnip1).text());
            Marker currentMarker = map.addMarker(option);
            directionsMarker.put(String.valueOf(wayPointId), currentMarker);

        }
        if ((++wayPointId) != lastWayPointId) {
            MarkerOptions option2 = new MarkerOptions();
            option2.position(bound2);
            option2.title("Waypoint " + wayPointId);
            option2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            option2.visible(true);
            option2.snippet(Jsoup.parse(htmlSnip2).text());
            Marker currentMarker2 = map.addMarker(option2);
            directionsMarker.put(String.valueOf(wayPointId), currentMarker2);
        }
        updateCameraTwoBounds(bound1, bound2, false);
    }


    public void setInvisibleDirectionsMarkers() {
        if (!directionsMarker.isEmpty()) {
            for (String current : directionsMarker.keySet()) {
                Marker currentMarker = directionsMarker.get(current);
                currentMarker.setVisible(false);
            }
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

        //if distance are bigger that maxDistance or coordinates from model object are not initialized, send it to the server
        if ((lat1 == 0.0d && long1 == 0.0d) || calculateDistance(lat1, long1, lat2, long2) > MAX_DISTANCE) {
            CoordinatesHandler handler = new CoordinatesHandler(activity, TravelActivity.user, this, String.valueOf(TravelActivity.travel.getId()));
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

    public double calculateDistance(double lat1, double long1, double lat2, double long2) {
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

    public void drawPolyLineDirections(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(10);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route
        if (directionsPolyLine != null) {
            directionsPolyLine.remove();
            directionsPolyLine = null;
        }
        directionsPolyLine = map.addPolyline(lineOptions);
        directionsPolyLine.setVisible(TravelActivity.directions.hideShowDirections.isChecked());

    }
}